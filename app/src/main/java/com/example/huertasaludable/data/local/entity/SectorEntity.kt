package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.EstadoSector

@Entity(tableName = "sectores")
data class SectorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: EstadoSector,
    val responsableId: Long? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)
