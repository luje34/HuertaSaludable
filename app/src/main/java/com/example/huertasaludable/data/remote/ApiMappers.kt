package com.example.huertasaludable.data.remote

import com.example.huertasaludable.data.local.entity.CultivoEntity
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

internal fun UserDto.toEntity(synchronizedWithApi: Boolean = true) = UsuarioEntity(
    id = id,
    nombre = nombre,
    email = email,
    legacyCredential = "",
    rol = Rol.valueOf(rol),
    tallerGrupo = tallerGrupo,
    activo = activo,
    synchronizedWithApi = synchronizedWithApi
)

internal fun SectorDto.toEntity() = SectorEntity(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    ubicacion = ubicacion.orEmpty(),
    estado = EstadoSector.valueOf(estado),
    responsableId = responsableId,
    fechaCreacion = requireNotNull(fechaCreacion) { "La API no devolvió fechaCreacion" }
        .let(ApiDateCodec::epochMillisFromLocalDateTime),
    synchronizedWithApi = true
)

internal fun SectorEntity.toRequestDto() = SectorRequestDto(
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado.name,
    responsableId = responsableId
)

internal fun CultivoDto.toEntity() = CultivoEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    sectorId = sectorId,
    fechaSiembra = requireNotNull(fechaSiembra) { "La API no devolvió fechaSiembra" }
        .let(ApiDateCodec::epochMillisFromLocalDate),
    fechaTrasplante = ApiDateCodec.nullableEpochMillisFromLocalDate(fechaTrasplante),
    fechaEstimadaCosecha = requireNotNull(fechaEstimadaCosecha) {
        "La API no devolvió fechaEstimadaCosecha"
    }.let(ApiDateCodec::epochMillisFromLocalDate),
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = EstadoCultivo.valueOf(estado),
    observaciones = observaciones.orEmpty(),
    fotografiaUri = fotografiaUri
)

internal fun CultivoEntity.toRequestDto() = CultivoRequestDto(
    nombre = nombre,
    descripcion = descripcion,
    sectorId = sectorId,
    fechaSiembra = ApiDateCodec.localDateFromEpochMillis(fechaSiembra),
    fechaTrasplante = ApiDateCodec.nullableLocalDateFromEpochMillis(fechaTrasplante),
    fechaEstimadaCosecha = ApiDateCodec.localDateFromEpochMillis(fechaEstimadaCosecha),
    frecuenciaRiegoDias = frecuenciaRiegoDias,
    estado = estado.name,
    observaciones = observaciones,
    fotografiaUri = fotografiaUri
)

internal fun TareaDto.toEntity() = TareaEntity(
    id = id,
    tipo = TipoTarea.valueOf(tipo),
    descripcion = descripcion,
    sectorId = sectorId,
    cultivoId = cultivoId,
    fechaProgramada = ApiDateCodec.epochMillisFromLocalDate(fechaProgramada),
    fechaRealizacion = ApiDateCodec.nullableEpochMillisFromLocalDate(fechaRealizacion),
    estudianteId = estudianteId,
    docenteCreadorId = docenteCreadorId,
    estado = EstadoTarea.valueOf(estado),
    comentarioEstudiante = comentarioEstudiante,
    comentarioDocente = comentarioDocente,
    fotografiaEvidenciaUri = fotografiaEvidenciaUri,
    fechaValidacion = ApiDateCodec.nullableEpochMillisFromLocalDateTime(fechaValidacion),
    docenteValidadorId = docenteValidadorId
)

internal fun TareaEntity.toRequestDto() = TareaRequestDto(
    tipo = tipo.name,
    descripcion = descripcion,
    sectorId = sectorId,
    cultivoId = cultivoId,
    fechaProgramada = ApiDateCodec.localDateFromEpochMillis(fechaProgramada),
    fechaRealizacion = ApiDateCodec.nullableLocalDateFromEpochMillis(fechaRealizacion),
    estudianteId = estudianteId,
    docenteCreadorId = docenteCreadorId,
    estado = estado.name,
    comentarioEstudiante = comentarioEstudiante,
    comentarioDocente = comentarioDocente,
    fotografiaEvidenciaUri = fotografiaEvidenciaUri,
    fechaValidacion = ApiDateCodec.nullableLocalDateTimeFromEpochMillis(fechaValidacion),
    docenteValidadorId = docenteValidadorId
)

internal fun ReporteProblemaDto.toEntity() = ReporteProblemaEntity(
    id = id,
    sectorId = sectorId,
    cultivoId = cultivoId,
    tipo = TipoProblema.valueOf(tipo),
    descripcion = descripcion,
    fechaReporte = ApiDateCodec.epochMillisFromLocalDate(fechaReporte),
    usuarioReportadorId = usuarioReportadorId,
    fotografiaUri = fotografiaUri,
    estado = EstadoProblema.valueOf(estado),
    comentarioDocente = comentarioDocente,
    fechaAtencion = ApiDateCodec.nullableEpochMillisFromLocalDate(fechaAtencion)
)

internal fun ReporteProblemaEntity.toCreateRequestDto() = ReporteProblemaCreateRequestDto(
    sectorId = sectorId,
    cultivoId = cultivoId,
    tipo = tipo.name,
    descripcion = descripcion,
    fotografiaUri = fotografiaUri
)

internal fun ReporteProblemaEntity.toUpdateRequestDto() = ReporteProblemaUpdateRequestDto(
    sectorId = sectorId,
    cultivoId = cultivoId,
    tipo = tipo.name,
    descripcion = descripcion,
    fechaReporte = ApiDateCodec.localDateFromEpochMillis(fechaReporte),
    usuarioReportadorId = usuarioReportadorId,
    fotografiaUri = fotografiaUri,
    estado = estado.name,
    comentarioDocente = comentarioDocente,
    fechaAtencion = ApiDateCodec.nullableLocalDateFromEpochMillis(fechaAtencion)
)
