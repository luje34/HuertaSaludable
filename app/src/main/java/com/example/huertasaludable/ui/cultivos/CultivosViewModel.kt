package com.example.huertasaludable.ui.cultivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.domain.validation.CultivoFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.session.UserSessionManager

class CultivosViewModel(private val repository: HuertaRepository) : ViewModel() {
    private var activeFormKey: Pair<Long, Long>? = null

    private fun esAdministradorAutenticado(usuarioId: Long): Boolean{
        val usuario = UserSessionManager.currentUser.value ?: return false
        return usuario.id == usuarioId && usuario.rol == Rol.ADMIN
    }

    val cultivos = repository.getCultivos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sectores = repository.getSectores().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(CultivoFormUiState())
    val formState: StateFlow<CultivoFormUiState> = _formState.asStateFlow()

    fun cargarCultivoParaEditar(cultivoId: Long, sectorIdInicial: Long = 0L) {
        val formKey = cultivoId to sectorIdInicial
        if (activeFormKey == formKey) return
        activeFormKey = formKey
        if (cultivoId == 0L) {
            _formState.value = CultivoFormUiState(sectorId = sectorIdInicial)
            return
        }
        viewModelScope.launch {
            val cultivo = repository.getCultivoById(cultivoId).first()
            if (cultivo != null && activeFormKey == formKey) {
                _formState.value = CultivoFormUiState(
                    id = cultivo.id,
                    nombre = cultivo.nombre,
                    descripcion = cultivo.descripcion,
                    sectorId = cultivo.sectorId,
                    fechaSiembra = cultivo.fechaSiembra,
                    fechaEstimadaCosecha = cultivo.fechaEstimadaCosecha,
                    frecuenciaRiegoDiasStr = cultivo.frecuenciaRiegoDias.toString(),
                    estado = cultivo.estado,
                    observaciones = cultivo.observaciones,
                    fotografiaUri = cultivo.fotografiaUri
                )
            }
        }
    }

    fun onNombreChanged(value: String) {
        _formState.update {
            it.copy(
                nombre = value,
                nombreError = null,
                generalError = null
            )
        }
    }

    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(
                descripcion = value,
                descripcionError = null,
                generalError = null
            )
        }
    }

    fun onSectorSelected(sectorId: Long) {
        _formState.update { it.copy(sectorId = sectorId, sectorError = null) }
    }

    fun onFechaSiembraChanged(fecha: Long) {
        _formState.update { it.copy(fechaSiembra = fecha, fechaError = null) }
    }

    fun onFechaCosechaChanged(fecha: Long) {
        _formState.update { it.copy(fechaEstimadaCosecha = fecha, fechaError = null) }
    }

    fun onFrecuenciaRiegoChanged(value: String) {
        _formState.update { it.copy(frecuenciaRiegoDiasStr = value, riegoError = null) }
    }

    fun onEstadoChanged(value: EstadoCultivo) {
        _formState.update { it.copy(estado = value) }
    }

    fun onObservacionesChanged(value: String) {
        _formState.update { it.copy(observaciones = value) }
    }

    fun onFotografiaSelected(uriString: String?) {
        _formState.update { it.copy(fotografiaUri = uriString) }
    }

    fun guardarCultivo(usuarioId: Long) {
        if (!esAdministradorAutenticado(usuarioId)) {
            _formState.update { it.copy(generalError = "No tienes permiso para guardar cultivos") }
            return
        }
        val state = _formState.value
        val nombre = state.nombre.trim()
        val descripcion = state.descripcion.trim()
        val riegoDias = state.frecuenciaRiegoDiasStr.toIntOrNull()
        val errors = CultivoFormValidator.validate(
            nombre = nombre,
            descripcion = descripcion,
            sectorId = state.sectorId,
            fechaSiembra = state.fechaSiembra,
            fechaEstimadaCosecha = state.fechaEstimadaCosecha,
            frecuenciaRiegoDias = state.frecuenciaRiegoDiasStr
        )
        _formState.update {
            it.copy(
                nombreError = errors.nombre,
                descripcionError = errors.descripcion,
                sectorError = errors.sector,
                fechaError = errors.fecha,
                riegoError = errors.riego
            )
        }
        if (errors.hasErrors || state.isLoading) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val cultivo = CultivoEntity(
                id = state.id,
                nombre = nombre,
                descripcion = descripcion,
                sectorId = state.sectorId,
                fechaSiembra = state.fechaSiembra,
                fechaEstimadaCosecha = state.fechaEstimadaCosecha,
                frecuenciaRiegoDias = checkNotNull(riegoDias),
                estado = state.estado,
                observaciones = state.observaciones,
                fotografiaUri = state.fotografiaUri
            )

            try {
                if (state.id == 0L) {
                    val newId = repository.insertCultivo(cultivo)
                    repository.registrarHistorial(
                        tipoEntidad = "CULTIVO",
                        entidadId = newId,
                        accion = "CREAR",
                        descripcion = "Cultivo '$nombre' registrado.",
                        usuarioId = usuarioId
                    )
                } else {
                    repository.updateCultivo(cultivo)
                    repository.registrarHistorial(
                        tipoEntidad = "CULTIVO",
                        entidadId = state.id,
                        accion = "ACTUALIZAR",
                        descripcion = "Cultivo '$nombre' actualizado.",
                        usuarioId = usuarioId
                    )
                }
                _formState.update { it.copy(isLoading = false, isSaved = true, generalError = null) }
            } catch (error: Exception) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "No se pudo guardar el cultivo: ${error.message ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    fun limpiarFormulario() {
        activeFormKey = null
        _formState.value = CultivoFormUiState()
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CultivosViewModel(repository) as T
        }
    }
}
