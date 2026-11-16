package com.example.huertasaludable.ui.problemas

import com.example.huertasaludable.data.model.TipoProblema

data class ReporteFormUiState(
    val tipo: TipoProblema = TipoProblema.FALTA_AGUA,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val cultivoError: String? = null,
    val fotografiaUri: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val generalError: String? = null
)
