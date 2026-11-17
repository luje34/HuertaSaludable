package com.example.huertasaludable.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE synchronizedWithApi = 1 ORDER BY nombre ASC")
    fun getUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id AND synchronizedWithApi = 1")
    fun getUsuarioById(id: Long): Flow<UsuarioEntity?>

    @Query("UPDATE usuarios SET synchronizedWithApi = 0")
    suspend fun markAllAsLocal()

    @Query(
        """
        DELETE FROM usuarios
        WHERE synchronizedWithApi = 0
          AND id NOT IN (SELECT usuarioId FROM historial_actividades)
          AND id NOT IN (SELECT responsableId FROM sectores WHERE responsableId IS NOT NULL)
          AND id NOT IN (SELECT estudianteId FROM tareas WHERE estudianteId IS NOT NULL)
          AND id NOT IN (SELECT docenteCreadorId FROM tareas)
          AND id NOT IN (SELECT docenteValidadorId FROM tareas WHERE docenteValidadorId IS NOT NULL)
          AND id NOT IN (SELECT usuarioReportadorId FROM reportes_problemas)
        """
    )
    suspend fun deleteUnusedLocalUsers()

    @Upsert
    suspend fun upsertUsuarios(usuarios: List<UsuarioEntity>)
}
