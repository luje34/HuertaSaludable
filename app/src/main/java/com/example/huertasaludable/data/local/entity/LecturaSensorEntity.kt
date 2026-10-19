package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lecturas_sensores",
    foreignKeys = [
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectorId")]
)
data class LecturaSensorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sectorId: Long,
    val fechaHora: Long = System.currentTimeMillis(),
    val humedadSueloPct: Float,
    val temperaturaAmbientalC: Float,
    val humedadAmbientalPct: Float,
    val origen: String = "DATOS_SIMULADOS_PRUEBA",
    val estadoLectura: String = "Normal"
)
