package com.example.huertasaludable.ui.components

import com.example.huertasaludable.data.local.entity.CultivoEntity

fun filtrarCultivosPorSector(
    cultivos: List<CultivoEntity>,
    sectorId: Long?
): List<CultivoEntity> = if (sectorId == null) {
    cultivos
} else {
    cultivos.filter { it.sectorId == sectorId }
}
