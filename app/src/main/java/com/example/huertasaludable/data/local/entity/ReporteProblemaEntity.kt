package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.TipoProblema

@Entity(
    tableName = "reportes_problemas",
    foreignKeys = [
        ForeignKey(
            entity = SectorEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivoId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioReportadorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("sectorId"),
        Index("cultivoId"),
        Index("usuarioReportadorId")
    ]
)
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