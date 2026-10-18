package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialActividadDao {
    @Query("SELECT * FROM historial_actividades ORDER BY fecha DESC")
    fun getHistorial(): Flow<List<HistorialActividadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: HistorialActividadEntity): Long
}
