package com.example.huertasaludable.ui.cultivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.data.repository.HuertaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CultivoFormUiState(
    val id: Long = 0,
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val sectorId: Long = 0L,
    val sectorError: String? = null,
    val fechaSiembra: Long = System.currentTimeMillis(),
    val fechaEstimadaCosecha: Long = System.currentTimeMillis() + (30 * 86400000L),
    val fechaError: String? = null,
    val frecuenciaRiegoDiasStr: String = "2",
    val riegoError: String? = null,
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fotografiaUri: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val generalError: String? = null
)

class CultivosViewModel(private val repository: HuertaRepository) : ViewModel() {

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
        if (cultivoId == 0L) {
            _formState.value = CultivoFormUiState(sectorId = sectorIdInicial)
            return
        }
        viewModelScope.launch {
            repository.getCultivoById(cultivoId).collect { cultivo ->
                if (cultivo != null) {
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
    }

    fun onNombreChanged(value: String) {
        _formState.update {
            it.copy(
                nombre = value,
                nombreError = if (value.isBlank()) "El nombre es obligatorio" else null,
                generalError = null
            )
        }
    }

    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(
                descripcion = value,
                descripcionError = if (value.isBlank()) "La descripción es obligatoria" else null,
                generalError = null
            )
        }
    }

    fun onSectorSelected(sectorId: Long) {
        _formState.update { it.copy(sectorId = sectorId, sectorError = null) }
    }

    fun onFechaSiembraChanged(fecha: Long) {
        _formState.update { state ->
            val fechaError = if (state.fechaEstimadaCosecha < fecha) {
                "La fecha de cosecha estimada no puede ser anterior a la siembra"
            } else null
            state.copy(fechaSiembra = fecha, fechaError = fechaError)
        }
    }

    fun onFechaCosechaChanged(fecha: Long) {
        _formState.update { state ->
            val fechaError = if (fecha < state.fechaSiembra) {
                "La fecha de cosecha estimada no puede ser anterior a la siembra"
            } else null
            state.copy(fechaEstimadaCosecha = fecha, fechaError = fechaError)
        }
    }

    fun onFrecuenciaRiegoChanged(value: String) {
        val num = value.toIntOrNull()
        val error = when {
            value.isBlank() -> "Ingresa los días de riego"
            num == null || num <= 0 -> "Debe ser un número mayor a 0"
            else -> null
        }
        _formState.update { it.copy(frecuenciaRiegoDiasStr = value, riegoError = error) }
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
        val state = _formState.value
        val nombre = state.nombre.trim()
        val descripcion = state.descripcion.trim()
        val riegoDias = state.frecuenciaRiegoDiasStr.toIntOrNull()

        var hasError = false
        if (nombre.isBlank()) {
            _formState.update { it.copy(nombreError = "Ingresa el nombre del cultivo") }
            hasError = true
        }
        if (descripcion.isBlank()) {
            _formState.update { it.copy(descripcionError = "Ingresa una descripción") }
            hasError = true
        }
        if (state.sectorId == 0L) {
            _formState.update { it.copy(sectorError = "Selecciona un sector") }
            hasError = true
        }
        if (riegoDias == null || riegoDias <= 0) {
            _formState.update { it.copy(riegoError = "La frecuencia de riego debe ser mayor a 0") }
            hasError = true
        }
        if (state.fechaEstimadaCosecha < state.fechaSiembra) {
            _formState.update { it.copy(fechaError = "La fecha de cosecha no puede ser anterior a la siembra") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val cultivo = CultivoEntity(
                id = state.id,
                nombre = nombre,
                descripcion = descripcion,
                sectorId = state.sectorId,
                fechaSiembra = state.fechaSiembra,
                fechaEstimadaCosecha = state.fechaEstimadaCosecha,
                frecuenciaRiegoDias = riegoDias!!,
                estado = state.estado,
                observaciones = state.observaciones,
                fotografiaUri = state.fotografiaUri
            )

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

            _formState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CultivosViewModel(repository) as T
        }
    }
}
