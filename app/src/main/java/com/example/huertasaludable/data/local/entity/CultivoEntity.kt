package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.EstadoCultivo

@Entity(
    tableName = "cultivos",
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
data class CultivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val sectorId: Long,
    val fechaSiembra: Long,
    val fechaTrasplante: Long? = null,
    val fechaEstimadaCosecha: Long,
    val frecuenciaRiegoDias: Int,
    val estado: EstadoCultivo,
    val observaciones: String = "",
    val fotografiaUri: String? = null
)
