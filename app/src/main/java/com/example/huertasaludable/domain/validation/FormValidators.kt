package com.example.huertasaludable.domain.validation

data class SectorFormErrors(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ubicacion: String? = null
) {
    val hasErrors: Boolean
        get() = codigo != null || nombre != null || descripcion != null || ubicacion != null
}

object SectorFormValidator {
    fun validate(codigo: String, nombre: String, descripcion: String, ubicacion: String) =
        SectorFormErrors(
            codigo = if (codigo.isBlank()) "Ingresa un código (ej: SEC-01)" else null,
            nombre = if (nombre.isBlank()) "Ingresa el nombre del sector" else null,
            descripcion = if (descripcion.isBlank()) "Ingresa una descripción del sector" else null,
            ubicacion = if (ubicacion.isBlank()) "Ingresa la ubicación física del sector" else null
        )
}

data class CultivoFormErrors(
    val nombre: String? = null,
    val descripcion: String? = null,
    val sector: String? = null,
    val fecha: String? = null,
    val riego: String? = null
) {
    val hasErrors: Boolean
        get() = nombre != null || descripcion != null || sector != null ||
            fecha != null || riego != null
}

object CultivoFormValidator {
    fun validate(
        nombre: String,
        descripcion: String,
        sectorId: Long,
        fechaSiembra: Long,
        fechaEstimadaCosecha: Long,
        frecuenciaRiegoDias: String
    ): CultivoFormErrors {
        val riegoDias = frecuenciaRiegoDias.toIntOrNull()
        return CultivoFormErrors(
            nombre = if (nombre.isBlank()) "Ingresa el nombre del cultivo" else null,
            descripcion = if (descripcion.isBlank()) "Ingresa una descripción" else null,
            sector = if (sectorId == 0L) "Selecciona un sector" else null,
            fecha = if (fechaEstimadaCosecha < fechaSiembra) {
                "La fecha de cosecha no puede ser anterior a la siembra"
            } else null,
            riego = if (riegoDias == null || riegoDias <= 0) {
                "La frecuencia de riego debe ser mayor a 0"
            } else null
        )
    }
}

data class TareaFormErrors(
    val descripcion: String? = null,
    val estudiante: String? = null,
    val cultivo: String? = null
) {
    val hasErrors: Boolean
        get() = descripcion != null || estudiante != null || cultivo != null
}

object TareaFormValidator {
    fun validate(
        descripcion: String,
        estudianteId: Long?,
        sectorId: Long?,
        cultivoSectorId: Long?
    ) = TareaFormErrors(
        descripcion = if (descripcion.isBlank()) "Ingresa las instrucciones de la tarea" else null,
        estudiante = if (estudianteId == null) "Selecciona el estudiante responsable" else null,
        cultivo = if (
            cultivoSectorId != null && sectorId != null && cultivoSectorId != sectorId
        ) {
            "El cultivo seleccionado no pertenece al sector"
        } else null
    )
}

object TareaDeliveryValidator {
    fun validate(comentario: String): String? =
        if (comentario.isBlank()) "Escribe un comentario de lo realizado" else null
}

object TareaReviewValidator {
    fun validateRejectionReason(motivo: String): String? =
        if (motivo.isBlank()) "Debes ingresar un motivo obligatorio para rechazar la tarea" else null
}

data class ReporteFormErrors(
    val descripcion: String? = null,
    val cultivo: String? = null
) {
    val hasErrors: Boolean
        get() = descripcion != null || cultivo != null
}

object ReporteFormValidator {
    fun validate(descripcion: String, sectorId: Long?, cultivoSectorId: Long?) =
        ReporteFormErrors(
            descripcion = if (descripcion.isBlank()) {
                "Ingresa la descripción del problema"
            } else null,
            cultivo = if (
                cultivoSectorId != null && sectorId != null && cultivoSectorId != sectorId
            ) {
                "El cultivo seleccionado no pertenece al sector"
            } else null
        )
}
