package com.example.huertasaludable

import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.domain.validation.CultivoFormValidator
import com.example.huertasaludable.domain.validation.SectorFormValidator
import com.example.huertasaludable.domain.validation.TareaFormValidator
import com.example.huertasaludable.ui.components.filtrarCultivosPorSector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidatorsTest {
    @Test
    fun cultivoListOnlyIncludesSelectedSector() {
        val cultivos = listOf(
            cultivo(id = 1L, sectorId = 10L),
            cultivo(id = 2L, sectorId = 20L)
        )

        assertTrue(filtrarCultivosPorSector(cultivos, 10L).map { it.id } == listOf(1L))
        assertTrue(filtrarCultivosPorSector(cultivos, null) == cultivos)
    }

    @Test
    fun tareaRequiresDescriptionAndResponsibleStudent() {
        val errors = TareaFormValidator.validate(
            descripcion = " ",
            estudianteId = null,
            sectorId = null,
            cultivoSectorId = null
        )

        assertTrue(errors.hasErrors)
        assertNotNull(errors.descripcion)
        assertNotNull(errors.estudiante)
    }

    @Test
    fun tareaRejectsCropFromAnotherSector() {
        val errors = TareaFormValidator.validate(
            descripcion = "Regar el cultivo",
            estudianteId = 3L,
            sectorId = 1L,
            cultivoSectorId = 2L
        )

        assertNotNull(errors.cultivo)
    }

    @Test
    fun tareaAllowsOptionalSectorForSelectedCrop() {
        val errors = TareaFormValidator.validate(
            descripcion = "Regar el cultivo",
            estudianteId = 3L,
            sectorId = null,
            cultivoSectorId = 2L
        )

        assertFalse(errors.hasErrors)
        assertNull(errors.cultivo)
    }

    @Test
    fun cultivoRejectsInvalidDatesAndWateringFrequency() {
        val errors = CultivoFormValidator.validate(
            nombre = "Tomate",
            descripcion = "Cultivo de verano",
            sectorId = 1L,
            fechaSiembra = 20L,
            fechaEstimadaCosecha = 10L,
            frecuenciaRiegoDias = "0"
        )

        assertNotNull(errors.fecha)
        assertNotNull(errors.riego)
    }

    @Test
    fun sectorRequiresEveryTextField() {
        val errors = SectorFormValidator.validate("", " ", " ", "")

        assertTrue(errors.hasErrors)
        assertNotNull(errors.codigo)
        assertNotNull(errors.nombre)
        assertNotNull(errors.descripcion)
        assertNotNull(errors.ubicacion)
    }

    private fun cultivo(id: Long, sectorId: Long) = CultivoEntity(
        id = id,
        nombre = "Cultivo $id",
        descripcion = "Descripcion",
        sectorId = sectorId,
        fechaSiembra = 1L,
        fechaEstimadaCosecha = 2L,
        frecuenciaRiegoDias = 1,
        estado = EstadoCultivo.PLANIFICADO
    )
}
