package com.example.huertasaludable.data.model

enum class EstadoTarea(val titulo: String) {
    PENDIENTE("Pendiente"),
    EN_DESARROLLO("En desarrollo"),
    PENDIENTE_VALIDACION("Pendiente de validación"),
    VALIDADA("Validada"),
    RECHAZADA("Rechazada"),
    ATRASADA("Atrasada")
}
