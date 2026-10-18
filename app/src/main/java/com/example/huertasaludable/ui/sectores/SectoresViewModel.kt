package com.example.huertasaludable.ui.sectores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.data.repository.HuertaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SectorFormUiState(
    val id: Long = 0,
    val codigo: String = "",
    val codigoError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val ubicacion: String = "",
    val ubicacionError: String? = null,
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val generalError: String? = null
)

class SectoresViewModel(private val repository: HuertaRepository) : ViewModel() {

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

    private val _formState = MutableStateFlow(SectorFormUiState())
    val formState: StateFlow<SectorFormUiState> = _formState.asStateFlow()

    fun cargarSectorParaEditar(sectorId: Long) {
        if (sectorId == 0L) {
            _formState.value = SectorFormUiState()
            return
        }
        viewModelScope.launch {
            repository.getSectorById(sectorId).collect { sector ->
                if (sector != null) {
                    _formState.value = SectorFormUiState(
                        id = sector.id,
                        codigo = sector.codigo,
                        nombre = sector.nombre,
                        descripcion = sector.descripcion,
                        ubicacion = sector.ubicacion,
                        estado = sector.estado
                    )
                }
            }
        }
    }

    fun onCodigoChanged(value: String) {
        _formState.update {
            it.copy(
                codigo = value,
                codigoError = if (value.isBlank()) "El código es obligatorio (ej. SEC-01)" else null,
                generalError = null
            )
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

    fun onUbicacionChanged(value: String) {
        _formState.update {
            it.copy(
                ubicacion = value,
                ubicacionError = if (value.isBlank()) "La ubicación es obligatoria" else null,
                generalError = null
            )
        }
    }

    fun onEstadoChanged(value: EstadoSector) {
        _formState.update { it.copy(estado = value) }
    }

    fun guardarSector(usuarioId: Long) {
        val state = _formState.value
        val codigo = state.codigo.trim()
        val nombre = state.nombre.trim()
        val descripcion = state.descripcion.trim()
        val ubicacion = state.ubicacion.trim()

        var hasError = false
        if (codigo.isBlank()) {
            _formState.update { it.copy(codigoError = "Ingresa un código (ej: SEC-01)") }
            hasError = true
        }
        if (nombre.isBlank()) {
            _formState.update { it.copy(nombreError = "Ingresa el nombre del sector") }
            hasError = true
        }
        if (descripcion.isBlank()) {
            _formState.update { it.copy(descripcionError = "Ingresa una descripción del sector") }
            hasError = true
        }
        if (ubicacion.isBlank()) {
            _formState.update { it.copy(ubicacionError = "Ingresa la ubicación física del sector") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val sector = SectorEntity(
                id = state.id,
                codigo = codigo,
                nombre = nombre,
                descripcion = descripcion,
                ubicacion = ubicacion,
                estado = state.estado,
                responsableId = usuarioId
            )

            if (state.id == 0L) {
                val newId = repository.insertSector(sector)
                repository.registrarHistorial(
                    tipoEntidad = "SECTOR",
                    entidadId = newId,
                    accion = "CREAR",
                    descripcion = "Sector '$nombre' ($codigo) creado.",
                    usuarioId = usuarioId
                )
            } else {
                repository.updateSector(sector)
                repository.registrarHistorial(
                    tipoEntidad = "SECTOR",
                    entidadId = state.id,
                    accion = "ACTUALIZAR",
                    descripcion = "Sector '$nombre' ($codigo) actualizado.",
                    usuarioId = usuarioId
                )
            }

            _formState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun cambiarEstadoSector(sector: SectorEntity, nuevoEstado: EstadoSector, usuarioId: Long) {
        viewModelScope.launch {
            val sectorActualizado = sector.copy(estado = nuevoEstado)
            repository.updateSector(sectorActualizado)
            repository.registrarHistorial(
                tipoEntidad = "SECTOR",
                entidadId = sector.id,
                accion = "CAMBIO_ESTADO",
                descripcion = "Estado del sector '${sector.nombre}' cambiado a ${nuevoEstado.titulo}.",
                usuarioId = usuarioId
            )
        }
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SectoresViewModel(repository) as T
        }
    }
}
