package com.example.huertasaludable.data.model

enum class TipoProblema(val titulo: String) {
    FALTA_AGUA("Falta de agua"),
    DANO_PLANTA("Daño en la planta"),
    PLAGA_SIMULADA("Plaga simulada"),
    DETERIORO_SECTOR("Deterioro del sector"),
    MANTENIMIENTO("Problema de mantenimiento"),
    OTRO("Otro problema")
}
