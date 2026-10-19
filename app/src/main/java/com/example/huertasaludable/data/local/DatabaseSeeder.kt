package com.example.huertasaludable.data.local

import com.example.huertasaludable.data.local.entity.CultivoEntity
import com.example.huertasaludable.data.local.entity.HistorialActividadEntity
import com.example.huertasaludable.data.local.entity.LecturaSensorEntity
import com.example.huertasaludable.data.local.entity.ReporteProblemaEntity
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.data.model.EstadoProblema
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.data.model.TipoProblema
import com.example.huertasaludable.data.model.TipoTarea

object DatabaseSeeder {

    suspend fun seed(db: AppDatabase) {
        if (db.usuarioDao().getUsuarioByEmail("docente@huerta.cl") != null) return
        val usuarioDao = db.usuarioDao()
        val sectorDao = db.sectorDao()
        val cultivoDao = db.cultivoDao()
        val tareaDao = db.tareaDao()
        val reporteDao = db.reporteProblemaDao()
        val sensorDao = db.lecturaSensorDao()
        val historialDao = db.historialActividadDao()

        // 1. Precargar 4 Cuentas de Prueba
        val usuarios = listOf(
            UsuarioEntity(
                id = 1,
                nombre = "Prof. Pablo Saldaña",
                email = "docente@huerta.cl",
                password = "123",
                rol = Rol.ADMIN,
                tallerGrupo = "Docente Coordinador CITT",
                activo = true
            ),
            UsuarioEntity(
                id = 2,
                nombre = "Camila Muñoz",
                email = "estudiante1@huerta.cl",
                password = "123",
                rol = Rol.COLABORADOR,
                tallerGrupo = "Taller 5° Básico A",
                activo = true
            ),
            UsuarioEntity(
                id = 3,
                nombre = "Mateo Silva",
                email = "estudiante2@huerta.cl",
                password = "123",
                rol = Rol.COLABORADOR,
                tallerGrupo = "Taller 6° Básico B",
                activo = true
            ),
            UsuarioEntity(
                id = 4,
                nombre = "Sofía Rojas",
                email = "estudiante3@huerta.cl",
                password = "123",
                rol = Rol.COLABORADOR,
                tallerGrupo = "Taller 7° Básico A",
                activo = true
            )
        )
        usuarioDao.insertUsuarios(usuarios)

        // 2. Precargar Sectores
        val sectores = listOf(
            SectorEntity(
                id = 1,
                codigo = "SEC-01",
                nombre = "Invernadero Norte",
                descripcion = "Invernadero cubierto con control de humedad para germinación y cultivos delicados.",
                ubicacion = "Zona A - Patio Norte de la Escuela",
                estado = EstadoSector.EN_USO,
                responsableId = 1
            ),
            SectorEntity(
                id = 2,
                codigo = "SEC-02",
                nombre = "Bancales Sur",
                descripcion = "Bancales elevados de madera para siembra directa de hortalizas y tubérculos.",
                ubicacion = "Zona B - Patio Sur de la Escuela",
                estado = EstadoSector.EN_USO,
                responsableId = 1
            )
        )
        sectorDao.insertSectores(sectores)

        // 3. Precargar Cultivos
        val now = System.currentTimeMillis()
        val unDiaMs = 86400000L
        val cultivos = listOf(
            CultivoEntity(
                id = 1,
                nombre = "Tomate Cherry",
                descripcion = "Variedad orgánicamente cultivada de fruto dulce y crecimiento rápido.",
                sectorId = 1,
                fechaSiembra = now - (15 * unDiaMs),
                fechaEstimadaCosecha = now + (30 * unDiaMs),
                frecuenciaRiegoDias = 2,
                estado = EstadoCultivo.EN_CRECIMIENTO,
                observaciones = "Requiere tutorado para mantener tallos erguidos."
            ),
            CultivoEntity(
                id = 2,
                nombre = "Lechuga Costina",
                descripcion = "Hortaliza de hojas verdes y crujientes.",
                sectorId = 2,
                fechaSiembra = now - (5 * unDiaMs),
                fechaEstimadaCosecha = now + (20 * unDiaMs),
                frecuenciaRiegoDias = 1,
                estado = EstadoCultivo.SEMBRADO,
                observaciones = "Proteger del sol directo durante el mediodía."
            ),
            CultivoEntity(
                id = 3,
                nombre = "Zanahoria Chantenay",
                descripcion = "Raíz cónica y gruesa rica en carotenos.",
                sectorId = 2,
                fechaSiembra = now - (25 * unDiaMs),
                fechaEstimadaCosecha = now + (35 * unDiaMs),
                frecuenciaRiegoDias = 3,
                estado = EstadoCultivo.EN_CRECIMIENTO,
                observaciones = "Suelo suelto para evitar malformación en raíz."
            )
        )
        cultivoDao.insertCultivos(cultivos)

        // 4. Precargar Tareas
        val tareas = listOf(
            TareaEntity(
                id = 1,
                tipo = TipoTarea.RIEGO,
                descripcion = "Regar el bancal de Tomates Cherry con regadera de lluvia fina.",
                sectorId = 1,
                cultivoId = 1,
                fechaProgramada = now + unDiaMs,
                estudianteId = 2,
                docenteCreadorId = 1,
                estado = EstadoTarea.PENDIENTE
            ),
            TareaEntity(
                id = 2,
                tipo = TipoTarea.RETIRO_MALEZAS,
                descripcion = "Retirar malezas pequeñas alrededor del bancal de Lechugas.",
                sectorId = 2,
                cultivoId = 2,
                fechaProgramada = now,
                estudianteId = 3,
                docenteCreadorId = 1,
                estado = EstadoTarea.EN_DESARROLLO
            ),
            TareaEntity(
                id = 3,
                tipo = TipoTarea.LIMPIEZA,
                descripcion = "Limpieza de herramientas y orden del Sector Invernadero Norte.",
                sectorId = 1,
                cultivoId = 1,
                fechaProgramada = now - unDiaMs,
                fechaRealizacion = now - (unDiaMs / 2),
                estudianteId = 4,
                docenteCreadorId = 1,
                estado = EstadoTarea.PENDIENTE_VALIDACION,
                comentarioEstudiante = "Herramientas limpiadas y guardadas en el estante de pruebas.",
                fotografiaEvidenciaUri = null
            )
        )
        tareaDao.insertTareas(tareas)

        // 5. Precargar Reporte de Problemas
        val reportes = listOf(
            ReporteProblemaEntity(
                id = 1,
                sectorId = 2,
                cultivoId = 2,
                tipo = TipoProblema.FALTA_AGUA,
                descripcion = "Las hojas de la lechuga se observan ligeramente marchitas al final de la tarde.",
                fechaReporte = now - (unDiaMs / 3),
                usuarioReportadorId = 2,
                estado = EstadoProblema.INFORMADO
            )
        )
        reporteDao.insertReportes(reportes)

        // 6. Precargar Lecturas Simuladas de Sensores
        val lecturas = listOf(
            LecturaSensorEntity(
                id = 1,
                sectorId = 1,
                fechaHora = now,
                humedadSueloPct = 65.5f,
                temperaturaAmbientalC = 22.4f,
                humedadAmbientalPct = 58.0f,
                origen = "SIMULADOR_PRUEBA",
                estadoLectura = "Normal"
            ),
            LecturaSensorEntity(
                id = 2,
                sectorId = 2,
                fechaHora = now,
                humedadSueloPct = 42.0f,
                temperaturaAmbientalC = 24.8f,
                humedadAmbientalPct = 50.0f,
                origen = "SIMULADOR_PRUEBA",
                estadoLectura = "Bajo Riego Sugerido"
            )
        )
        sensorDao.insertLecturas(lecturas)

        // 7. Precargar Historial
        historialDao.insertHistorial(
            HistorialActividadEntity(
                id = 1,
                tipoEntidad = "SISTEMA",
                entidadId = 0,
                accion = "INICIALIZACION",
                descripcion = "Base de datos inicializada con datos de prueba ficticios para el MVP.",
                fecha = now,
                usuarioId = 1
            )
        )
    }
}
