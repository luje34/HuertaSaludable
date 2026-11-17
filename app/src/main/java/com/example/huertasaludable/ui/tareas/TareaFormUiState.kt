package com.example.huertasaludable.ui.tareas

import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.TipoTarea

data class TareaFormUiState(
    val id: Long = 0,
    val tipo: TipoTarea = TipoTarea.RIEGO,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val cultivoError: String? = null,
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
    val isDelivered: Boolean = false,
    val generalError: String? = null
)

data class TareaReviewUiState(
    val tareaId: Long = 0,
    val comentarioDocente: String = "",
    val motivoRechazoError: String? = null,
    val isLoading: Boolean = false,
    val isReviewed: Boolean = false,
    val generalError: String? = null
)
