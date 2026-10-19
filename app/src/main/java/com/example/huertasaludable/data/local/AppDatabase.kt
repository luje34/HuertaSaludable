package com.example.huertasaludable.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.huertasaludable.data.local.dao.CultivoDao
import com.example.huertasaludable.data.local.dao.HistorialActividadDao
import com.example.huertasaludable.data.local.dao.LecturaSensorDao
import com.example.huertasaludable.data.local.dao.ReporteProblemaDao
import com.example.huertasaludable.data.local.dao.SectorDao
import com.example.huertasaludable.data.local.dao.TareaDao
import com.example.huertasaludable.data.local.dao.UsuarioDao
import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        SectorEntity::class,
        CultivoEntity::class,
        TareaEntity::class,
        ReporteProblemaEntity::class,
        LecturaSensorEntity::class,
        HistorialActividadEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun reporteProblemaDao(): ReporteProblemaDao
    abstract fun lecturaSensorDao(): LecturaSensorDao
    abstract fun historialActividadDao(): HistorialActividadDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                arrayOf(
                    "CREATE TEMP TABLE migration_sectores AS SELECT * FROM sectores",
                    "CREATE TEMP TABLE migration_cultivos AS SELECT * FROM cultivos",
                    "CREATE TEMP TABLE migration_tareas AS SELECT * FROM tareas",
                    "CREATE TEMP TABLE migration_reportes AS SELECT * FROM reportes_problemas",
                    "CREATE TEMP TABLE migration_lecturas AS SELECT * FROM lecturas_sensores",
                    "CREATE TEMP TABLE migration_historial AS SELECT * FROM historial_actividades",
                    "DROP TABLE tareas",
                    "DROP TABLE reportes_problemas",
                    "DROP TABLE lecturas_sensores",
                    "DROP TABLE historial_actividades",
                    "DROP TABLE cultivos",
                    "DROP TABLE sectores",
                    """
            CREATE TABLE sectores (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                ubicacion TEXT NOT NULL,
                estado TEXT NOT NULL,
                responsableId INTEGER,
                fechaCreacion INTEGER NOT NULL,
                FOREIGN KEY (responsableId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
                    "CREATE INDEX index_sectores_responsableId ON sectores(responsableId)",
                    """
            CREATE TABLE cultivos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                sectorId INTEGER NOT NULL,
                fechaSiembra INTEGER NOT NULL,
                fechaTrasplante INTEGER,
                fechaEstimadaCosecha INTEGER NOT NULL,
                frecuenciaRiegoDias INTEGER NOT NULL,
                estado TEXT NOT NULL,
                observaciones TEXT NOT NULL,
                fotografiaUri TEXT,
                FOREIGN KEY (sectorId) REFERENCES sectores(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
                    "CREATE INDEX index_cultivos_sectorId ON cultivos(sectorId)",
                    """
            CREATE TABLE tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tipo TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                sectorId INTEGER,
                cultivoId INTEGER,
                fechaProgramada INTEGER NOT NULL,
                fechaRealizacion INTEGER,
                estudianteId INTEGER,
                docenteCreadorId INTEGER NOT NULL,
                estado TEXT NOT NULL,
                comentarioEstudiante TEXT,
                comentarioDocente TEXT,
                fotografiaEvidenciaUri TEXT,
                fechaValidacion INTEGER,
                docenteValidadorId INTEGER,
                FOREIGN KEY (sectorId) REFERENCES sectores(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY (cultivoId) REFERENCES cultivos(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY (estudianteId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY (docenteCreadorId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY (docenteValidadorId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
                    "CREATE INDEX index_tareas_sectorId ON tareas(sectorId)",
                    "CREATE INDEX index_tareas_cultivoId ON tareas(cultivoId)",
                    "CREATE INDEX index_tareas_estudianteId ON tareas(estudianteId)",
                    "CREATE INDEX index_tareas_docenteCreadorId ON tareas(docenteCreadorId)",
                    "CREATE INDEX index_tareas_docenteValidadorId ON tareas(docenteValidadorId)",
                    """
            CREATE TABLE reportes_problemas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                sectorId INTEGER,
                cultivoId INTEGER,
                tipo TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                fechaReporte INTEGER NOT NULL,
                usuarioReportadorId INTEGER NOT NULL,
                fotografiaUri TEXT,
                estado TEXT NOT NULL,
                comentarioDocente TEXT,
                fechaAtencion INTEGER,
                FOREIGN KEY (sectorId) REFERENCES sectores(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY (cultivoId) REFERENCES cultivos(id)
                    ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY (usuarioReportadorId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
                    "CREATE INDEX index_reportes_problemas_sectorId ON reportes_problemas(sectorId)",
                    "CREATE INDEX index_reportes_problemas_cultivoId ON reportes_problemas(cultivoId)",
                    "CREATE INDEX index_reportes_problemas_usuarioReportadorId ON reportes_problemas(usuarioReportadorId)",
                    """
            CREATE TABLE lecturas_sensores (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                sectorId INTEGER NOT NULL,
                fechaHora INTEGER NOT NULL,
                humedadSueloPct REAL NOT NULL,
                temperaturaAmbientalC REAL NOT NULL,
                humedadAmbientalPct REAL NOT NULL,
                origen TEXT NOT NULL,
                estadoLectura TEXT NOT NULL,
                FOREIGN KEY (sectorId) REFERENCES sectores(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
                    "CREATE INDEX index_lecturas_sensores_sectorId ON lecturas_sensores(sectorId)",
                    """
            CREATE TABLE historial_actividades (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tipoEntidad TEXT NOT NULL,
                entidadId INTEGER NOT NULL,
                accion TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                fecha INTEGER NOT NULL,
                usuarioId INTEGER NOT NULL,
                FOREIGN KEY (usuarioId) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
                    "CREATE INDEX index_historial_actividades_usuarioId ON historial_actividades(usuarioId)",
                    """
            INSERT INTO sectores (
                id, codigo, nombre, descripcion, ubicacion, estado, responsableId, fechaCreacion
            )
            SELECT id, codigo, nombre, descripcion, ubicacion, estado,
                CASE WHEN EXISTS (
                    SELECT 1 FROM usuarios WHERE usuarios.id = migration_sectores.responsableId
                ) THEN responsableId ELSE NULL END,
                fechaCreacion
            FROM migration_sectores
            """.trimIndent(),
                    """
            INSERT INTO cultivos (
                id, nombre, descripcion, sectorId, fechaSiembra, fechaTrasplante,
                fechaEstimadaCosecha, frecuenciaRiegoDias, estado, observaciones, fotografiaUri
            )
            SELECT id, nombre, descripcion, sectorId, fechaSiembra, fechaTrasplante,
                fechaEstimadaCosecha, frecuenciaRiegoDias, estado, observaciones, fotografiaUri
            FROM migration_cultivos
            """.trimIndent(),
                    """
            INSERT INTO tareas (
                id, tipo, descripcion, sectorId, cultivoId, fechaProgramada, fechaRealizacion,
                estudianteId, docenteCreadorId, estado, comentarioEstudiante, comentarioDocente,
                fotografiaEvidenciaUri, fechaValidacion, docenteValidadorId
            )
            SELECT id, tipo, descripcion,
                CASE WHEN EXISTS (
                    SELECT 1 FROM sectores WHERE sectores.id = migration_tareas.sectorId
                ) THEN sectorId ELSE NULL END,
                CASE WHEN EXISTS (
                    SELECT 1 FROM cultivos WHERE cultivos.id = migration_tareas.cultivoId
                ) THEN cultivoId ELSE NULL END,
                fechaProgramada, fechaRealizacion,
                CASE WHEN EXISTS (
                    SELECT 1 FROM usuarios WHERE usuarios.id = migration_tareas.estudianteId
                ) THEN estudianteId ELSE NULL END,
                docenteCreadorId, estado, comentarioEstudiante, comentarioDocente,
                fotografiaEvidenciaUri, fechaValidacion,
                CASE WHEN EXISTS (
                    SELECT 1 FROM usuarios WHERE usuarios.id = migration_tareas.docenteValidadorId
                ) THEN docenteValidadorId ELSE NULL END
            FROM migration_tareas
            """.trimIndent(),
                    """
            INSERT INTO reportes_problemas (
                id, sectorId, cultivoId, tipo, descripcion, fechaReporte, usuarioReportadorId,
                fotografiaUri, estado, comentarioDocente, fechaAtencion
            )
            SELECT id,
                CASE WHEN EXISTS (
                    SELECT 1 FROM sectores WHERE sectores.id = migration_reportes.sectorId
                ) THEN sectorId ELSE NULL END,
                CASE WHEN EXISTS (
                    SELECT 1 FROM cultivos WHERE cultivos.id = migration_reportes.cultivoId
                ) THEN cultivoId ELSE NULL END,
                tipo, descripcion, fechaReporte, usuarioReportadorId,
                fotografiaUri, estado, comentarioDocente, fechaAtencion
            FROM migration_reportes
            """.trimIndent(),
                    """
            INSERT INTO lecturas_sensores (
                id, sectorId, fechaHora, humedadSueloPct, temperaturaAmbientalC,
                humedadAmbientalPct, origen, estadoLectura
            )
            SELECT id, sectorId, fechaHora, humedadSueloPct, temperaturaAmbientalC,
                humedadAmbientalPct, origen, estadoLectura
            FROM migration_lecturas
            """.trimIndent(),
                    """
            INSERT INTO historial_actividades (
                id, tipoEntidad, entidadId, accion, descripcion, fecha, usuarioId
            )
            SELECT id, tipoEntidad, entidadId, accion, descripcion, fecha, usuarioId
            FROM migration_historial
            """.trimIndent(),
                    "DROP TABLE migration_sectores",
                    "DROP TABLE migration_cultivos",
                    "DROP TABLE migration_tareas",
                    "DROP TABLE migration_reportes",
                    "DROP TABLE migration_lecturas",
                    "DROP TABLE migration_historial"
                ).forEach { sql -> db.execSQL(sql) }
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huerta_saludable_db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
