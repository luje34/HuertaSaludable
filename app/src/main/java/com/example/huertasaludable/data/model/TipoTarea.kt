package com.example.huertasaludable.data.model

enum class TipoTarea(val titulo: String) {
    PREPARACION_SECTOR("Preparación del sector"),
    SIEMBRA("Siembra"),
    TRASPLANTE("Trasplante"),
    RIEGO("Riego"),
    LIMPIEZA("Limpieza"),
    RETIRO_MALEZAS("Retiro de malezas"),
    OBSERVACION("Observación del cultivo"),
    MANTENIMIENTO("Mantenimiento"),
    REVISION_SUELO("Revisión del suelo"),
    REGISTRO_CRECIMIENTO("Registro de crecimiento"),
    COSECHA("Cosecha"),
    OTRO("Otra actividad")
}
