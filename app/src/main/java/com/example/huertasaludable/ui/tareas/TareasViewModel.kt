package com.example.huertasaludable.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.TipoTarea
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

data class TareaFormUiState(
    val id: Long = 0,
    val tipo: TipoTarea = TipoTarea.RIEGO,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val fechaProgramada: Long = System.currentTimeMillis() + 86400000L,
    val estudianteId: Long? = null,
    val estudianteError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val generalError: String? = null
)

data class TareaDeliveryUiState(
    val tareaId: Long = 0,
    val comentarioEstudiante: String = "",
    val comentarioError: String? = null,
    val fotografiaEvidenciaUri: String? = null,
    val fotografiaError: String? = null,
    val isLoading: Boolean = false,
    val isDelivered: Boolean = false
)

data class TareaReviewUiState(
    val tareaId: Long = 0,
    val comentarioDocente: String = "",
    val motivoRechazoError: String? = null,
    val isLoading: Boolean = false,
    val isReviewed: Boolean = false
)

class TareasViewModel(private val repository: HuertaRepository) : ViewModel() {

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
        if (tareaId == 0L) {
            _formState.value = TareaFormUiState()
            return
        }
        viewModelScope.launch {
            repository.getTareaById(tareaId).collect { tarea ->
                if (tarea != null) {
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
    }

    fun onTipoChanged(tipo: TipoTarea) {
        _formState.update { it.copy(tipo = tipo) }
    }
    fun onFechaProgramadaChanged(fecha: Long) {
        _formState.update { it.copy(fechaProgramada = fecha) }
    }    fun onDescripcionChanged(value: String) {
        _formState.update {
            it.copy(
                descripcion = value,
                descripcionError = if (value.isBlank()) "La descripción es obligatoria" else null
            )
        }
    }

    fun onSectorSelected(sectorId: Long?) {
        _formState.update { it.copy(sectorId = sectorId) }
    }

    fun onCultivoSelected(cultivoId: Long?) {
        _formState.update { it.copy(cultivoId = cultivoId) }
    }

    fun onEstudianteSelected(estudianteId: Long?) {
        _formState.update {
            it.copy(
                estudianteId = estudianteId,
                estudianteError = if (estudianteId == null) "Selecciona un estudiante responsable" else null
            )
        }
    }

    fun guardarTarea(docenteId: Long) {
        val state = _formState.value
        val descripcion = state.descripcion.trim()

        var hasError = false
        if (descripcion.isBlank()) {
            _formState.update { it.copy(descripcionError = "Ingresa las instrucciones de la tarea") }
            hasError = true
        }
        if (state.estudianteId == null) {
            _formState.update { it.copy(estudianteError = "Selecciona el estudiante responsable") }
            hasError = true
        }

        if (hasError) return

        if (!esAdministradorAutenticado(docenteId)) return
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val tarea = TareaEntity(
                id = state.id,
                tipo = state.tipo,
                descripcion = descripcion,
                sectorId = state.sectorId,
                cultivoId = state.cultivoId,
                fechaProgramada = state.fechaProgramada,
                estudianteId = state.estudianteId,
                docenteCreadorId = docenteId,
                estado = EstadoTarea.PENDIENTE
            )

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

            _formState.update { it.copy(isLoading = false, isSaved = true) }
        }
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
        _deliveryState.value = TareaDeliveryUiState(tareaId = tareaId)
    }

    fun onComentarioEstudianteChanged(value: String) {
        _deliveryState.update {
            it.copy(
                comentarioEstudiante = value,
                comentarioError = if (value.isBlank()) "Escribe un comentario sobre el trabajo realizado" else null
            )
        }
    }

    fun onFotografiaEvidenciaSelected(uriString: String?) {
        _deliveryState.update { it.copy(fotografiaEvidenciaUri = uriString) }
    }

    fun entregarTarea(tarea: TareaEntity, usuarioId: Long) {
        val state = _deliveryState.value
        val comentario = state.comentarioEstudiante.trim()

        var hasError = false
        if (comentario.isBlank()) {
            _deliveryState.update { it.copy(comentarioError = "Escribe un comentario de lo realizado") }
            hasError = true
        }

        if (hasError) return

        if(!esColaboradorAsignado(tarea, usuarioId) ||
            (tarea.estado != EstadoTarea.EN_DESARROLLO &&
                    tarea.estado != EstadoTarea.RECHAZADA)
            )return
        viewModelScope.launch {
            _deliveryState.update { it.copy(isLoading = true) }
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
            _deliveryState.update { it.copy(isLoading = false, isDelivered = true) }
        }
    }

    fun cargarTareaParaRevision(tareaId: Long) {
        _reviewState.value = TareaReviewUiState(tareaId = tareaId)
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
        if (motivo.isBlank()) {
            _reviewState.update { it.copy(motivoRechazoError = "Debes ingresar un motivo obligatorio para rechazar la tarea") }
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
