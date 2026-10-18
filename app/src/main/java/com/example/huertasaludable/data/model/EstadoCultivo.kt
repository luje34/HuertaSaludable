package com.example.huertasaludable.data.model

enum class EstadoCultivo(val titulo: String) {
    PLANIFICADO("Planificado"),
    SEMBRADO("Sembrado"),
    EN_CRECIMIENTO("En crecimiento"),
    LISTO_PARA_COSECHAR("Listo para cosechar"),
    COSECHADO("Cosechado"),
    CON_PROBLEMAS("Con problemas"),
    FINALIZADO("Finalizado")
}
