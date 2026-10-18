package com.example.huertasaludable.data.repository

import com.example.huertasaludable.data.local.AppDatabase
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class HuertaRepository(private val db: AppDatabase) {

    // --- Usuarios ---
    fun getUsuarios(): Flow<List<UsuarioEntity>> = db.usuarioDao().getUsuarios()
    fun getUsuarioById(id: Long): Flow<UsuarioEntity?> = db.usuarioDao().getUsuarioById(id)
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity? = db.usuarioDao().getUsuarioByEmail(email)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long = db.usuarioDao().insertUsuario(usuario)
    suspend fun updateUsuario(usuario: UsuarioEntity) = db.usuarioDao().updateUsuario(usuario)

    // --- Sectores ---
    fun getSectores(): Flow<List<SectorEntity>> = db.sectorDao().getSectores()
    fun getSectorById(id: Long): Flow<SectorEntity?> = db.sectorDao().getSectorById(id)
    suspend fun insertSector(sector: SectorEntity): Long = db.sectorDao().insertSector(sector)
    suspend fun updateSector(sector: SectorEntity) = db.sectorDao().updateSector(sector)
    suspend fun deleteSector(sector: SectorEntity) = db.sectorDao().deleteSector(sector)

    // --- Cultivos ---
    fun getCultivos(): Flow<List<CultivoEntity>> = db.cultivoDao().getCultivos()
    fun getCultivosBySector(sectorId: Long): Flow<List<CultivoEntity>> = db.cultivoDao().getCultivosBySector(sectorId)
    fun getCultivoById(id: Long): Flow<CultivoEntity?> = db.cultivoDao().getCultivoById(id)
    suspend fun insertCultivo(cultivo: CultivoEntity): Long = db.cultivoDao().insertCultivo(cultivo)
    suspend fun updateCultivo(cultivo: CultivoEntity) = db.cultivoDao().updateCultivo(cultivo)
    suspend fun deleteCultivo(cultivo: CultivoEntity) = db.cultivoDao().deleteCultivo(cultivo)

    // --- Tareas ---
    fun getTareas(): Flow<List<TareaEntity>> = db.tareaDao().getTareas()
    fun getTareasByEstudiante(estudianteId: Long): Flow<List<TareaEntity>> = db.tareaDao().getTareasByEstudiante(estudianteId)
    fun getTareaById(id: Long): Flow<TareaEntity?> = db.tareaDao().getTareaById(id)
    suspend fun insertTarea(tarea: TareaEntity): Long = db.tareaDao().insertTarea(tarea)
    suspend fun updateTarea(tarea: TareaEntity) = db.tareaDao().updateTarea(tarea)
    suspend fun deleteTarea(tarea: TareaEntity) = db.tareaDao().deleteTarea(tarea)

    // --- Reportes de Problemas ---
    fun getReportes(): Flow<List<ReporteProblemaEntity>> = db.reporteProblemaDao().getReportes()
    fun getReporteById(id: Long): Flow<ReporteProblemaEntity?> = db.reporteProblemaDao().getReporteById(id)
    suspend fun insertReporte(reporte: ReporteProblemaEntity): Long = db.reporteProblemaDao().insertReporte(reporte)
    suspend fun updateReporte(reporte: ReporteProblemaEntity) = db.reporteProblemaDao().updateReporte(reporte)

    // --- Sensores Simulados ---
    fun getLecturas(): Flow<List<LecturaSensorEntity>> = db.lecturaSensorDao().getLecturas()
    fun getLecturasBySector(sectorId: Long): Flow<List<LecturaSensorEntity>> = db.lecturaSensorDao().getLecturasBySector(sectorId)
    suspend fun insertLectura(lectura: LecturaSensorEntity): Long = db.lecturaSensorDao().insertLectura(lectura)

    // --- Historial ---
    fun getHistorial(): Flow<List<HistorialActividadEntity>> = db.historialActividadDao().getHistorial()
    suspend fun registrarHistorial(
        tipoEntidad: String,
        entidadId: Long,
        accion: String,
        descripcion: String,
        usuarioId: Long
    ): Long {
        return db.historialActividadDao().insertHistorial(
            HistorialActividadEntity(
                tipoEntidad = tipoEntidad,
                entidadId = entidadId,
                accion = accion,
                descripcion = descripcion,
                usuarioId = usuarioId
            )
        )
    }
}
