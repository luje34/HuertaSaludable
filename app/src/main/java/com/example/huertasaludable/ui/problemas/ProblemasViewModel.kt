package com.example.huertasaludable.ui.problemas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.TipoProblema
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.domain.validation.ReporteFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.session.UserSessionManager

class ProblemasViewModel(private val repository: HuertaRepository) : ViewModel() {
    private var isReportFormActive = false

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

    val sectoresParaSensores = repository.getTodosLosSectores().stateIn(
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
        _formState.update { it.copy(descripcion = value, descripcionError = null, generalError = null) }
    }

    fun onSectorSelected(sectorId: Long?) {
        _formState.update { state ->
            state.copy(
                sectorId = sectorId,
                cultivoId = if (sectorId != null && sectorId != state.sectorId) null else state.cultivoId,
                cultivoError = null,
                generalError = null
            )
        }
    }

    fun onCultivoSelected(cultivoId: Long?) {
        _formState.update { it.copy(cultivoId = cultivoId, cultivoError = null) }
    }

    fun onFotografiaSelected(uriString: String?) {
        _formState.update { it.copy(fotografiaUri = uriString) }
    }

    fun cargarNuevoReporte() {
        if (isReportFormActive) return
        isReportFormActive = true
        _formState.value = ReporteFormUiState()
    }

    fun limpiarFormularioReporte() {
        isReportFormActive = false
        _formState.value = ReporteFormUiState()
    }

    fun guardarReporte(usuarioId: Long) {
        if (UserSessionManager.currentUser.value?.id != usuarioId) {
            _formState.update { it.copy(generalError = "No tienes permiso para reportar problemas") }
            return
        }
        val state = _formState.value
        val descripcion = state.descripcion.trim()
        val selectedCropSectorId = state.cultivoId?.let { cropId ->
            cultivos.value.firstOrNull { it.id == cropId }?.sectorId
        }
        val errors = ReporteFormValidator.validate(descripcion, state.sectorId, selectedCropSectorId)
        _formState.update {
            it.copy(
                descripcionError = errors.descripcion,
                cultivoError = errors.cultivo,
                generalError = null
            )
        }
        if (errors.hasErrors || state.isLoading) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            try {
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
            } catch (error: Exception) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "No se pudo enviar el reporte: ${error.localizedMessage ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    fun cambiarEstadoReporte(reporte: ReporteProblemaEntity, nuevoEstado: EstadoProblema, comentarioDocente: String?, usuarioId: Long) {
        if (!esAdministradorAutenticado(usuarioId))return
        viewModelScope.launch {
            try {
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
                _formState.update { it.copy(generalError = null) }
            } catch (error: Exception) {
                _formState.update {
                    it.copy(generalError = "No se pudo actualizar el reporte: ${error.message ?: "error desconocido"}")
                }
            }
        }
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProblemasViewModel(repository) as T
        }
    }
}
