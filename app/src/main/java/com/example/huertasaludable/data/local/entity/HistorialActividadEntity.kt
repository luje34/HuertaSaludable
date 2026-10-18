package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_actividades")
data class HistorialActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoEntidad: String,
    val entidadId: Long,
    val accion: String,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis(),
    val usuarioId: Long
)
