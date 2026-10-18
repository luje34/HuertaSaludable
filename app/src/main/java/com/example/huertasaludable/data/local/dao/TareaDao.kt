package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.huertasaludable.data.local.entity.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas ORDER BY fechaProgramada ASC")
    fun getTareas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE estudianteId = :estudianteId ORDER BY fechaProgramada ASC")
    fun getTareasByEstudiante(estudianteId: Long): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    fun getTareaById(id: Long): Flow<TareaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTareas(tareas: List<TareaEntity>)

    @Update
    suspend fun updateTarea(tarea: TareaEntity)

    @Delete
    suspend fun deleteTarea(tarea: TareaEntity)
}
