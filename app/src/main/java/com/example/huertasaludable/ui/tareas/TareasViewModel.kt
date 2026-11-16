package com.example.huertasaludable.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.TipoTarea
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.domain.validation.TareaDeliveryValidator
import com.example.huertasaludable.domain.validation.TareaFormValidator
import com.example.huertasaludable.domain.validation.TareaReviewValidator
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

class TareasViewModel(private val repository: HuertaRepository) : ViewModel() {
    private var activeFormId: Long? = null
    private var activeDeliveryTaskId: Long? = null
    private var activeReviewTaskId: Long? = null

    private fun esAdministradorAutenticado(usuarioId: Long): Boolean{
        val usuario = UserSessionManager.currentUser.value ?: return false
        return usuario.id == usuarioId && usuario.rol == Rol.ADMIN
    }

    private fun esColaboradorAsignado(tarea: TareaEntity, usuarioId: Long):Boolean{
        val usuario = UserSessionManager.currentUser.value ?: return false
        return usuario.id == usuarioId&&
                usuario.rol == Rol.COLABORADOR &&
                tarea.estudianteId == usuario.id
    }

    val tareas = repository.getTareas().stateIn(
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

    val usuarios = repository.getUsuarios().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(TareaFormUiState())
    val formState: StateFlow<TareaFormUiState> = _formState.asStateFlow()

    private val _deliveryState = MutableStateFlow(TareaDeliveryUiState())
    val deliveryState: StateFlow<TareaDeliveryUiState> = _deliveryState.asStateFlow()

    private val _reviewState = MutableStateFlow(TareaReviewUiState())
    val reviewState: StateFlow<TareaReviewUiState> = _reviewState.asStateFlow()

    fun cargarTareaParaEditar(tareaId: Long) {
        if (activeFormId == tareaId) return
        activeFormId = tareaId
        if (tareaId == 0L) {
            _formState.value = TareaFormUiState()
            return
        }
        viewModelScope.launch {
            val tarea = repository.getTareaById(tareaId).first()
            if (tarea != null && activeFormId == tareaId) {
                _formState.value = TareaFormUiState(
                    id = tarea.id,
                    tipo = tarea.tipo,
                    descripcion = tarea.descripcion,
                    sectorId = tarea.sectorId,
                    cultivoId = tarea.cultivoId,
                    fechaProgramada = tarea.fechaProgramada,
                    estudianteId = tarea.estudianteId
                )
            }
        }
    }

    fun onTipoChanged(tipo: TipoTarea) {
        _formState.update { it.copy(tipo = tipo, generalError = null) }
    }
    fun onFechaProgramadaChanged(fecha: Long) {
        _formState.update { it.copy(fechaProgramada = fecha, generalError = null) }
    }    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(
                descripcion = value,
                descripcionError = if (value.isBlank()) "La descripción es obligatoria" else null
            )
        }
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
        _formState.update {
            it.copy(cultivoId = cultivoId, cultivoError = null, generalError = null)
        }
    }

    fun onEstudianteSelected(estudianteId: Long?) {
        _formState.update {
            it.copy(
                estudianteId = estudianteId,
                estudianteError = null,
                generalError = null
            )
        }
    }

    fun guardarTarea(docenteId: Long) {
        if (!esAdministradorAutenticado(docenteId)) {
            _formState.update { it.copy(generalError = "No tienes permiso para asignar tareas") }
            return
        }
        val state = _formState.value
        val descripcion = state.descripcion.trim()
        val selectedCropSectorId = state.cultivoId?.let { cropId ->
            cultivos.value.firstOrNull { it.id == cropId }?.sectorId
        }
        val errors = TareaFormValidator.validate(
            descripcion = descripcion,
            estudianteId = state.estudianteId,
            sectorId = state.sectorId,
            cultivoSectorId = selectedCropSectorId
        )
        _formState.update {
            it.copy(
                descripcionError = errors.descripcion,
                estudianteError = errors.estudiante,
                cultivoError = errors.cultivo,
                generalError = null
            )
        }
        if (errors.hasErrors || state.isLoading) return
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            try {
            val tarea = if (state.id == 0L) {
                TareaEntity(
                    tipo = state.tipo,
                    descripcion = descripcion,
                    sectorId = state.sectorId,
                    cultivoId = state.cultivoId,
                    fechaProgramada = state.fechaProgramada,
                    estudianteId = state.estudianteId,
                    docenteCreadorId = docenteId,
                    estado = EstadoTarea.PENDIENTE
                )
            } else {
                repository.getTareaById(state.id).first()?.copy(
                    tipo = state.tipo,
                    descripcion = descripcion,
                    sectorId = state.sectorId,
                    cultivoId = state.cultivoId,
                    fechaProgramada = state.fechaProgramada,
                    estudianteId = state.estudianteId
                ) ?: error("No se encontró la tarea que se intenta editar")
            }

            if (state.id == 0L) {
                val newId = repository.insertTarea(tarea)
                repository.registrarHistorial(
                    tipoEntidad = "TAREA",
                    entidadId = newId,
                    accion = "CREAR",
                    descripcion = "Tarea '${state.tipo.titulo}' asignada.",
                    usuarioId = docenteId
                )
            } else {
                repository.updateTarea(tarea)
            }
            } catch (error: Exception) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "No se pudo guardar la tarea: ${error.localizedMessage ?: "error desconocido"}"
                    )
                }
                return@launch
            }

            _formState.update {
                it.copy(isLoading = false, isSaved = true, generalError = null)
            }
        }
    }

    fun limpiarFormulario() {
        activeFormId = null
        _formState.value = TareaFormUiState()
    }

    fun iniciarTarea(tarea: TareaEntity, usuarioId: Long) {
        if (!esColaboradorAsignado(tarea, usuarioId) ||
            tarea.estado != EstadoTarea.PENDIENTE
        )return
        viewModelScope.launch {
            val tareaActualizada = tarea.copy(
                estado = EstadoTarea.EN_DESARROLLO,
                fechaRealizacion = System.currentTimeMillis()
            )
            repository.updateTarea(tareaActualizada)
            repository.registrarHistorial(
                tipoEntidad = "TAREA",
                entidadId = tarea.id,
                accion = "INICIAR",
                descripcion = "Estudiante inició la tarea '${tarea.tipo.titulo}'.",
                usuarioId = usuarioId
            )
        }
    }

    fun cargarTareaParaEntrega(tareaId: Long) {
        if (activeDeliveryTaskId == tareaId) return
        activeDeliveryTaskId = tareaId
        _deliveryState.value = TareaDeliveryUiState(tareaId = tareaId)
    }

    fun limpiarEntrega() {
        activeDeliveryTaskId = null
        _deliveryState.value = TareaDeliveryUiState()
    }

    fun onComentarioEstudianteChanged(value: String) {
        _deliveryState.update {
            it.copy(
                comentarioEstudiante = value,
                comentarioError = null,
                generalError = null
            )
        }
    }

    fun onFotografiaEvidenciaSelected(uriString: String?) {
        _deliveryState.update { it.copy(fotografiaEvidenciaUri = uriString) }
    }

    fun entregarTarea(tarea: TareaEntity, usuarioId: Long) {
        val state = _deliveryState.value
        val comentario = state.comentarioEstudiante.trim()

        val comentarioError = TareaDeliveryValidator.validate(comentario)
        if (comentarioError != null) {
            _deliveryState.update { it.copy(comentarioError = comentarioError) }
            return
        }

        if (!esColaboradorAsignado(tarea, usuarioId) ||
            (tarea.estado != EstadoTarea.EN_DESARROLLO &&
                tarea.estado != EstadoTarea.RECHAZADA)
        ) {
            _deliveryState.update {
                it.copy(generalError = "No puedes entregar esta tarea en su estado actual")
            }
            return
        }
        if (state.isLoading) return
        viewModelScope.launch {
            _deliveryState.update { it.copy(isLoading = true) }
            try {
            val tareaActualizada = tarea.copy(
                estado = EstadoTarea.PENDIENTE_VALIDACION,
                comentarioEstudiante = comentario,
                fotografiaEvidenciaUri = state.fotografiaEvidenciaUri,
                fechaRealizacion = System.currentTimeMillis()
            )
            repository.updateTarea(tareaActualizada)
            repository.registrarHistorial(
                tipoEntidad = "TAREA",
                entidadId = tarea.id,
                accion = "ENTREGAR",
                descripcion = "Estudiante entregó evidencia de la tarea '${tarea.tipo.titulo}'.",
                usuarioId = usuarioId
            )
            _deliveryState.update {
                it.copy(isLoading = false, isDelivered = true, generalError = null)
            }
            } catch (error: Exception) {
                _deliveryState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "No se pudo enviar la tarea: ${error.localizedMessage ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    fun cargarTareaParaRevision(tareaId: Long) {
        if (activeReviewTaskId == tareaId) return
        activeReviewTaskId = tareaId
        _reviewState.value = TareaReviewUiState(tareaId = tareaId)
    }

    fun limpiarRevision() {
        activeReviewTaskId = null
        _reviewState.value = TareaReviewUiState()
    }

    fun onComentarioDocenteChanged(value: String) {
        _reviewState.update { it.copy(comentarioDocente = value, motivoRechazoError = null) }
    }

    fun aprobarTarea(tarea: TareaEntity, docenteId: Long) {
        if (!esAdministradorAutenticado(docenteId) ||
            tarea.estudianteId == docenteId ||
            tarea.estado != EstadoTarea.PENDIENTE_VALIDACION
        )return
        viewModelScope.launch {
            _reviewState.update { it.copy(isLoading = true) }
            val tareaActualizada = tarea.copy(
                estado = EstadoTarea.VALIDADA,
                comentarioDocente = _reviewState.value.comentarioDocente.ifBlank { "Trabajo validado correctamente." },
                fechaValidacion = System.currentTimeMillis(),
                docenteValidadorId = docenteId
            )
            repository.updateTarea(tareaActualizada)
            repository.registrarHistorial(
                tipoEntidad = "TAREA",
                entidadId = tarea.id,
                accion = "VALIDAR",
                descripcion = "Docente aprobó la tarea '${tarea.tipo.titulo}'.",
                usuarioId = docenteId
            )
            _reviewState.update { it.copy(isLoading = false, isReviewed = true) }
        }
    }

    fun rechazarTarea(tarea: TareaEntity, docenteId: Long) {
        val motivo = _reviewState.value.comentarioDocente.trim()
        val motivoError = TareaReviewValidator.validateRejectionReason(motivo)
        if (motivoError != null) {
            _reviewState.update { it.copy(motivoRechazoError = motivoError) }
            return
        }
        if (!esAdministradorAutenticado(docenteId) ||
            tarea.estudianteId == docenteId ||
            tarea.estado != EstadoTarea.PENDIENTE_VALIDACION
        )return
        viewModelScope.launch {
            _reviewState.update { it.copy(isLoading = true) }
            val tareaActualizada = tarea.copy(
                estado = EstadoTarea.RECHAZADA,
                comentarioDocente = motivo,
                fechaValidacion = System.currentTimeMillis(),
                docenteValidadorId = docenteId
            )
            repository.updateTarea(tareaActualizada)
            repository.registrarHistorial(
                tipoEntidad = "TAREA",
                entidadId = tarea.id,
                accion = "RECHAZAR",
                descripcion = "Docente rechazó la tarea '${tarea.tipo.titulo}' por motivo: $motivo",
                usuarioId = docenteId
            )
            _reviewState.update { it.copy(isLoading = false, isReviewed = true) }
        }
    }

    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TareasViewModel(repository) as T
        }
    }
}
