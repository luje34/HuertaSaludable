package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaSensorDao {
    @Query("SELECT * FROM lecturas_sensores ORDER BY fechaHora DESC")
    fun getLecturas(): Flow<List<LecturaSensorEntity>>

    @Query("SELECT * FROM lecturas_sensores WHERE sectorId = :sectorId ORDER BY fechaHora DESC")
    fun getLecturasBySector(sectorId: Long): Flow<List<LecturaSensorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLectura(lectura: LecturaSensorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecturas(lecturas: List<LecturaSensorEntity>)
}
