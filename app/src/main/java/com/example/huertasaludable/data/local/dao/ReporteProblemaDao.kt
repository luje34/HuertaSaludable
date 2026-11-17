package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteProblemaDao {
    @Query("SELECT * FROM reportes_problemas ORDER BY fechaReporte DESC")
    fun getReportes(): Flow<List<ReporteProblemaEntity>>

    @Query("SELECT * FROM reportes_problemas WHERE id = :id")
    fun getReporteById(id: Long): Flow<ReporteProblemaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReporte(reporte: ReporteProblemaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportes(reportes: List<ReporteProblemaEntity>)

    @Update
    suspend fun updateReporte(reporte: ReporteProblemaEntity)

    @Delete
    suspend fun deleteReporte(reporte: ReporteProblemaEntity)

    @Query("DELETE FROM reportes_problemas")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertReporte(reporte: ReporteProblemaEntity)

    @Upsert
    suspend fun upsertReportes(reportes: List<ReporteProblemaEntity>)
}
