package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.TipoTarea

@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoTarea,
    val descripcion: String,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val fechaProgramada: Long,
    val fechaRealizacion: Long? = null,
    val estudianteId: Long? = null,
    val docenteCreadorId: Long,
    val estado: EstadoTarea,
    val comentarioEstudiante: String? = null,
    val comentarioDocente: String? = null,
    val fotografiaEvidenciaUri: String? = null,
    val fechaValidacion: Long? = null,
    val docenteValidadorId: Long? = null
)
