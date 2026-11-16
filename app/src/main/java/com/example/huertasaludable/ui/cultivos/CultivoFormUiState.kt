package com.example.huertasaludable.ui.cultivos

import com.example.huertasaludable.data.model.EstadoCultivo

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
