package com.example.huertasaludable.ui.sectores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.domain.validation.SectorFormValidator
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

class SectoresViewModel(private val repository: HuertaRepository) : ViewModel() {
    private var activeFormId: Long? = null

    private fun esAdministradorAutenticado(usuarioId: Long): Boolean{
        val usuario = UserSessionManager.currentUser.value ?: return false
        return usuario.id == usuarioId && usuario.rol == Rol.ADMIN
    }

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
        if (activeFormId == sectorId) return
        activeFormId = sectorId
        if (sectorId == 0L) {
            _formState.value = SectorFormUiState()
            return
        }
        viewModelScope.launch {
            val sector = repository.getSectorById(sectorId).first()
            if (sector != null && activeFormId == sectorId) {
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

    fun onCodigoChanged(value: String) {
        _formState.update { it.copy(codigo = value, codigoError = null, generalError = null) }
    }

    fun onNombreChanged(value: String) {
        _formState.update { it.copy(nombre = value, nombreError = null, generalError = null) }
    }

    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(descripcion = value, descripcionError = null, generalError = null)
        }
    }

    fun onUbicacionChanged(value: String) {
        _formState.update { it.copy(ubicacion = value, ubicacionError = null, generalError = null) }
    }

    fun onEstadoChanged(value: EstadoSector) {
        _formState.update { it.copy(estado = value) }
    }

    fun guardarSector(usuarioId: Long) {
        if (!esAdministradorAutenticado(usuarioId)) {
            _formState.update { it.copy(generalError = "No tienes permiso para guardar sectores") }
            return
        }
        val state = _formState.value
        val codigo = state.codigo.trim()
        val nombre = state.nombre.trim()
        val descripcion = state.descripcion.trim()
        val ubicacion = state.ubicacion.trim()
        val errors = SectorFormValidator.validate(codigo, nombre, descripcion, ubicacion)
        _formState.update {
            it.copy(
                codigoError = errors.codigo,
                nombreError = errors.nombre,
                descripcionError = errors.descripcion,
                ubicacionError = errors.ubicacion
            )
        }
        if (errors.hasErrors || state.isLoading) return

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

    fun limpiarFormulario() {
        activeFormId = null
        _formState.value = SectorFormUiState()
    }

    fun cambiarEstadoSector(sector: SectorEntity, nuevoEstado: EstadoSector, usuarioId: Long) {
        if (!esAdministradorAutenticado(usuarioId)) return
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
