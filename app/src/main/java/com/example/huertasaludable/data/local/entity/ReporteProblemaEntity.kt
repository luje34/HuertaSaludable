package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.TipoProblema

@Entity(tableName = "reportes_problemas")
data class ReporteProblemaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sectorId: Long? = null,
    val cultivoId: Long? = null,
    val tipo: TipoProblema,
    val descripcion: String,
    val fechaReporte: Long = System.currentTimeMillis(),
    val usuarioReportadorId: Long,
    val fotografiaUri: String? = null,
    val estado: EstadoProblema = EstadoProblema.INFORMADO,
    val comentarioDocente: String? = null,
    val fechaAtencion: Long? = null
)
