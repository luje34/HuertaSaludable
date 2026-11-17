package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.huertasaludable.data.local.entity.SectorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectores WHERE synchronizedWithApi = 1 ORDER BY codigo ASC")
    fun getSectores(): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectores WHERE id = :id AND synchronizedWithApi = 1")
    fun getSectorById(id: Long): Flow<SectorEntity?>

    @Query("SELECT * FROM sectores ORDER BY codigo ASC")
    fun getTodosLosSectores(): Flow<List<SectorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: SectorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectores(sectores: List<SectorEntity>)

    @Update
    suspend fun updateSector(sector: SectorEntity)

    @Delete
    suspend fun deleteSector(sector: SectorEntity)

    @Query("UPDATE sectores SET synchronizedWithApi = 0")
    suspend fun markAllAsLocal()

    @Query(
        "DELETE FROM sectores WHERE id NOT IN (SELECT DISTINCT sectorId FROM lecturas_sensores)"
    )
    suspend fun deleteSectoresSinLecturasLocales()

    @Upsert
    suspend fun upsertSector(sector: SectorEntity)

    @Upsert
    suspend fun upsertSectores(sectores: List<SectorEntity>)
}
