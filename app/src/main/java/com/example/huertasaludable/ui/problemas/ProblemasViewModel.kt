package com.example.huertasaludable.ui.problemas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.TipoProblema
import com.example.huertasaludable.data.repository.HuertaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.session.UserSessionManager

data class ReporteFormUiState(
    val tipo: TipoProblema = TipoProblema.FALTA_AGUA,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val fotografiaUri: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val generalError: String? = null
)

class ProblemasViewModel(private val repository: HuertaRepository) : ViewModel() {

    private fun esAdministradorAutenticado(usuarioId: Long): Boolean{
        val usuario = UserSessionManager.currentUser.value ?: return false
        return usuario.id == usuarioId && usuario.rol == Rol.ADMIN
    }
    val reportes = repository.getReportes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sectores = repository.getSectores().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cultivos = repository.getCultivos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lecturas = repository.getLecturas().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(ReporteFormUiState())
    val formState: StateFlow<ReporteFormUiState> = _formState.asStateFlow()

    fun onTipoChanged(tipo: TipoProblema) {
        _formState.update { it.copy(tipo = tipo) }
    }

    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(
                descripcion = value,
                descripcionError = if (value.isBlank()) "Describe el problema detectado" else null
            )
        }
    }

    fun onSectorSelected(sectorId: Long?) {
        _formState.update { it.copy(sectorId = sectorId) }
    }

    fun onCultivoSelected(cultivoId: Long?) {
        _formState.update { it.copy(cultivoId = cultivoId) }
    }

    fun onFotografiaSelected(uriString: String?) {
        _formState.update { it.copy(fotografiaUri = uriString) }
    }

    fun guardarReporte(usuarioId: Long) {
        if (UserSessionManager.currentUser.value?.id != usuarioId) return
        val state = _formState.value
        val descripcion = state.descripcion.trim()

        if (descripcion.isBlank()) {
            _formState.update { it.copy(descripcionError = "Ingresa la descripción del problema") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val reporte = ReporteProblemaEntity(
                sectorId = state.sectorId,
                cultivoId = state.cultivoId,
                tipo = state.tipo,
                descripcion = descripcion,
                usuarioReportadorId = usuarioId,
                fotografiaUri = state.fotografiaUri,
                estado = EstadoProblema.INFORMADO
            )
            val newId = repository.insertReporte(reporte)
            repository.registrarHistorial(
                tipoEntidad = "PROBLEMA",
                entidadId = newId,
                accion = "REPORTAR",
                descripcion = "Problema '${state.tipo.titulo}' reportado.",
                usuarioId = usuarioId
            )
            _formState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun cambiarEstadoReporte(reporte: ReporteProblemaEntity, nuevoEstado: EstadoProblema, comentarioDocente: String?, usuarioId: Long) {
        if (!esAdministradorAutenticado(usuarioId))return
        viewModelScope.launch {
            val reporteActualizado = reporte.copy(
                estado = nuevoEstado,
                comentarioDocente = comentarioDocente ?: reporte.comentarioDocente,
                fechaAtencion = System.currentTimeMillis()
            )
            repository.updateReporte(reporteActualizado)
            repository.registrarHistorial(
                tipoEntidad = "PROBLEMA",
                entidadId = reporte.id,
                accion = "CAMBIO_ESTADO",
                descripcion = "Estado del reporte de problema cambiado a ${nuevoEstado.titulo}.",
                usuarioId = usuarioId
            )
        }
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProblemasViewModel(repository) as T
        }
    }
}
