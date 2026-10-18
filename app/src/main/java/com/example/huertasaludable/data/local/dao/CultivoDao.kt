package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.huertasaludable.data.local.entity.CultivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {
    @Query("SELECT * FROM cultivos ORDER BY nombre ASC")
    fun getCultivos(): Flow<List<CultivoEntity>>

    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId ORDER BY nombre ASC")
    fun getCultivosBySector(sectorId: Long): Flow<List<CultivoEntity>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    fun getCultivoById(id: Long): Flow<CultivoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultivo(cultivo: CultivoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultivos(cultivos: List<CultivoEntity>)

    @Update
    suspend fun updateCultivo(cultivo: CultivoEntity)

    @Delete
    suspend fun deleteCultivo(cultivo: CultivoEntity)
}
