package com.example.huertasaludable.data.remote

import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.TipoTarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ApiDateCodecTest {
    @Test
    fun localDateUsesUtcCalendarDateAndMapsBackToUtcMidnight() {
        val source = Instant.parse("2026-11-17T23:45:12Z").toEpochMilli()

        assertEquals("2026-11-17", ApiDateCodec.localDateFromEpochMillis(source))
        assertEquals(
            Instant.parse("2026-11-17T00:00:00Z").toEpochMilli(),
            ApiDateCodec.epochMillisFromLocalDate("2026-11-17")
        )
        assertEquals(
            "2026-11-17T23:45:12",
            ApiDateCodec.localDateTimeFromEpochMillis(source)
        )
        assertEquals(source, ApiDateCodec.epochMillisFromLocalDateTime("2026-11-17T23:45:12"))
    }

    @Test
    fun localDateTimeAcceptsFractionalSecondsFromServer() {
        assertEquals(
            Instant.parse("2026-11-17T14:30:00.123Z").toEpochMilli(),
            ApiDateCodec.epochMillisFromLocalDateTime("2026-11-17T14:30:00.123")
        )
    }

    @Test
    fun taskDtoMapsEnumNamesAndNullableDates() {
        val task = TareaDto(
            id = 7,
            tipo = "RIEGO",
            descripcion = "Regar",
            sectorId = null,
            cultivoId = 4,
            fechaProgramada = "2026-11-18",
            fechaRealizacion = null,
            estudianteId = 2,
            docenteCreadorId = 1,
            estado = "PENDIENTE_VALIDACION",
            comentarioEstudiante = "Listo",
            comentarioDocente = null,
            fotografiaEvidenciaUri = "file:///local/evidencia.jpg",
            fechaValidacion = "2026-11-17T23:45:12",
            docenteValidadorId = 1
        ).toEntity()

        assertEquals(TipoTarea.RIEGO, task.tipo)
        assertEquals(EstadoTarea.PENDIENTE_VALIDACION, task.estado)
        assertEquals("2026-11-18", ApiDateCodec.localDateFromEpochMillis(task.fechaProgramada))
        assertNull(task.fechaRealizacion)
        assertEquals("2026-11-17T23:45:12", ApiDateCodec.localDateTimeFromEpochMillis(task.fechaValidacion!!))
        assertEquals("file:///local/evidencia.jpg", task.fotografiaEvidenciaUri)
    }
}
