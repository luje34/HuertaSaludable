package com.example.huertasaludable.ui.sectores

import com.example.huertasaludable.data.model.EstadoSector

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
