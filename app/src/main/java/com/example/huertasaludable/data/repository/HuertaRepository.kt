package com.example.huertasaludable.data.repository

import androidx.room.withTransaction
import com.example.huertasaludable.data.local.AppDatabase
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.data.remote.AccessTokenStore
import com.example.huertasaludable.data.remote.AuthResponseDto
import com.example.huertasaludable.data.remote.HuertaApiClient
import com.example.huertasaludable.data.remote.TareaAvanceRequestDto
import com.example.huertasaludable.data.remote.TareaDto
import com.example.huertasaludable.data.remote.UserDto
import com.example.huertasaludable.data.remote.toCreateRequestDto
import com.example.huertasaludable.data.remote.toEntity
import com.example.huertasaludable.data.remote.toRequestDto
import com.example.huertasaludable.data.remote.toUpdateRequestDto
import com.example.huertasaludable.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HuertaRepository(
    private val db: AppDatabase,
    private val api: HuertaApiClient,
    private val tokenStore: AccessTokenStore
) {

    suspend fun login(email: String, password: String): UsuarioEntity =
        finishAuthentication(api.login(email, password))

    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        tallerGrupo: String
    ): UsuarioEntity = finishAuthentication(api.register(nombre, email, password, tallerGrupo))

    private suspend fun finishAuthentication(auth: AuthResponseDto): UsuarioEntity {
        require(auth.tokenType.equals("Bearer", ignoreCase = true)) {
            "La API devolvió un tipo de token no compatible"
        }
        withContext(Dispatchers.IO) { tokenStore.save(auth.accessToken) }
        return try {
            val profile = api.profile().toEntity()
            refreshRemoteData(profile)
            profile
        } catch (error: Exception) {
            withContext(Dispatchers.IO) { tokenStore.clear() }
            throw error
        }
    }

    suspend fun refreshRemoteData() {
        refreshRemoteData(api.profile().toEntity())
    }

    private suspend fun refreshRemoteData(profile: UsuarioEntity) {
        val sectors = api.sectores()
        val crops = api.cultivos()
        val tasks = api.tareas()
        val reports = api.reportesProblemas()
        val serverUsers = if (profile.rol == Rol.ADMIN) {
            (api.usuarios().map { it.toEntity() } + profile).distinctBy { it.id }
        } else {
            listOf(profile)
        }
        val usersById = serverUsers.associateBy { it.id }.toMutableMap()
        val referencedUserIds = buildSet {
            sectors.mapNotNullTo(this) { it.responsableId }
            tasks.forEach { task ->
                add(task.estudianteId)
                add(task.docenteCreadorId)
                task.docenteValidadorId?.let(::add)
            }
            reports.forEach { add(it.usuarioReportadorId) }
        }
        referencedUserIds.forEach { id ->
            if (id !in usersById) {
                usersById[id] = UsuarioEntity(
                    id = id,
                    nombre = "Usuario de huerta",
                    email = "usuario-$id@local.invalid",
                    rol = Rol.COLABORADOR,
                    tallerGrupo = "",
                    synchronizedWithApi = false
                )
            }
        }

        db.withTransaction {
            db.tareaDao().deleteAll()
            db.reporteProblemaDao().deleteAll()
            db.cultivoDao().deleteAll()
            db.sectorDao().markAllAsLocal()
            db.usuarioDao().markAllAsLocal()
            db.sectorDao().deleteSectoresSinLecturasLocales()
            db.usuarioDao().deleteUnusedLocalUsers()

            db.usuarioDao().upsertUsuarios(usersById.values.toList())
            db.sectorDao().upsertSectores(sectors.map { it.toEntity() })
            db.cultivoDao().upsertCultivos(crops.map { it.toEntity() })
            db.tareaDao().upsertTareas(tasks.map(TareaDto::toEntity))
            db.reporteProblemaDao().upsertReportes(reports.map { it.toEntity() })
        }
    }

    fun logout() {
        tokenStore.clear()
    }

    // --- Usuarios ---
    fun getUsuarios(): Flow<List<UsuarioEntity>> = db.usuarioDao().getUsuarios()
    fun getUsuarioById(id: Long): Flow<UsuarioEntity?> = db.usuarioDao().getUsuarioById(id)

    // --- Sectores ---
    fun getSectores(): Flow<List<SectorEntity>> = db.sectorDao().getSectores()
    fun getTodosLosSectores(): Flow<List<SectorEntity>> = db.sectorDao().getTodosLosSectores()
    fun getSectorById(id: Long): Flow<SectorEntity?> = db.sectorDao().getSectorById(id)

    suspend fun insertSector(sector: SectorEntity): Long {
        val saved = api.crearSector(sector.toRequestDto()).toEntity()
        db.sectorDao().upsertSector(saved)
        return saved.id
    }

    suspend fun updateSector(sector: SectorEntity) {
        val saved = api.actualizarSector(sector.id, sector.toRequestDto()).toEntity()
        db.sectorDao().upsertSector(saved)
    }

    suspend fun deleteSector(sector: SectorEntity) {
        api.eliminarSector(sector.id)
        db.sectorDao().deleteSector(sector)
    }

    // --- Cultivos ---
    fun getCultivos(): Flow<List<CultivoEntity>> = db.cultivoDao().getCultivos()
    fun getCultivosBySector(sectorId: Long): Flow<List<CultivoEntity>> =
        db.cultivoDao().getCultivosBySector(sectorId)
    fun getCultivoById(id: Long): Flow<CultivoEntity?> = db.cultivoDao().getCultivoById(id)

    suspend fun insertCultivo(cultivo: CultivoEntity): Long {
        val saved = api.crearCultivo(cultivo.toRequestDto()).toEntity()
        db.cultivoDao().upsertCultivo(saved)
        return saved.id
    }

    suspend fun updateCultivo(cultivo: CultivoEntity) {
        val saved = api.actualizarCultivo(cultivo.id, cultivo.toRequestDto()).toEntity()
        db.cultivoDao().upsertCultivo(saved)
    }

    suspend fun deleteCultivo(cultivo: CultivoEntity) {
        api.eliminarCultivo(cultivo.id)
        db.cultivoDao().deleteCultivo(cultivo)
    }

    // --- Tareas ---
    fun getTareas(): Flow<List<TareaEntity>> = db.tareaDao().getTareas()
    fun getTareasByEstudiante(estudianteId: Long): Flow<List<TareaEntity>> =
        db.tareaDao().getTareasByEstudiante(estudianteId)
    fun getTareaById(id: Long): Flow<TareaEntity?> = db.tareaDao().getTareaById(id)

    suspend fun insertTarea(tarea: TareaEntity): Long {
        val saved = api.crearTarea(tarea.toRequestDto()).toEntity()
        db.tareaDao().upsertTarea(saved)
        return saved.id
    }

    suspend fun updateTarea(tarea: TareaEntity) {
        val saved = if (
            UserSessionManager.currentUser.value?.rol == Rol.COLABORADOR &&
            (tarea.estado == EstadoTarea.EN_DESARROLLO ||
                tarea.estado == EstadoTarea.PENDIENTE_VALIDACION)
        ) {
            api.actualizarAvance(
                tarea.id,
                TareaAvanceRequestDto(
                    estado = tarea.estado.name,
                    comentarioEstudiante = tarea.comentarioEstudiante,
                    fotografiaEvidenciaUri = tarea.fotografiaEvidenciaUri
                )
            )
        } else {
            api.actualizarTarea(tarea.id, tarea.toRequestDto())
        }.toEntity()
        db.tareaDao().upsertTarea(saved)
    }

    suspend fun deleteTarea(tarea: TareaEntity) {
        api.eliminarTarea(tarea.id)
        db.tareaDao().deleteTarea(tarea)
    }

    // --- Reportes de Problemas ---
    fun getReportes(): Flow<List<ReporteProblemaEntity>> = db.reporteProblemaDao().getReportes()
    fun getReporteById(id: Long): Flow<ReporteProblemaEntity?> =
        db.reporteProblemaDao().getReporteById(id)

    suspend fun insertReporte(reporte: ReporteProblemaEntity): Long {
        val saved = api.crearReporte(reporte.toCreateRequestDto()).toEntity()
        db.reporteProblemaDao().upsertReporte(saved)
        return saved.id
    }

    suspend fun updateReporte(reporte: ReporteProblemaEntity) {
        val saved = api.actualizarReporte(reporte.id, reporte.toUpdateRequestDto()).toEntity()
        db.reporteProblemaDao().upsertReporte(saved)
    }

    suspend fun deleteReporte(reporte: ReporteProblemaEntity) {
        api.eliminarReporte(reporte.id)
        db.reporteProblemaDao().deleteReporte(reporte)
    }

    // --- Sensores Simulados ---
    fun getLecturas(): Flow<List<LecturaSensorEntity>> = db.lecturaSensorDao().getLecturas()
    fun getLecturasBySector(sectorId: Long): Flow<List<LecturaSensorEntity>> =
        db.lecturaSensorDao().getLecturasBySector(sectorId)
    suspend fun insertLectura(lectura: LecturaSensorEntity): Long =
        db.lecturaSensorDao().insertLectura(lectura)

    // --- Historial local ---
    fun getHistorial(): Flow<List<HistorialActividadEntity>> =
        db.historialActividadDao().getHistorial()

    suspend fun registrarHistorial(
        tipoEntidad: String,
        entidadId: Long,
        accion: String,
        descripcion: String,
        usuarioId: Long
    ): Long = db.historialActividadDao().insertHistorial(
        HistorialActividadEntity(
            tipoEntidad = tipoEntidad,
            entidadId = entidadId,
            accion = accion,
            descripcion = descripcion,
            usuarioId = usuarioId
        )
    )
}
