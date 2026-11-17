package com.example.huertasaludable.data.remote

data class LoginRequestDto(val email: String, val password: String)

data class RegisterRequestDto(
    val nombre: String,
    val email: String,
    val password: String,
    val tallerGrupo: String
)

data class UserDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String,
    val tallerGrupo: String,
    val activo: Boolean
)

data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val usuario: UserDto
)

data class SectorDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val ubicacion: String?,
    val estado: String,
    val responsableId: Long?,
    val fechaCreacion: String?
)

data class SectorRequestDto(
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: String,
    val responsableId: Long?
)

data class CultivoDto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val sectorId: Long,
    val fechaSiembra: String?,
    val fechaTrasplante: String?,
    val fechaEstimadaCosecha: String?,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String?,
    val fotografiaUri: String?
)

data class CultivoRequestDto(
    val nombre: String,
    val descripcion: String,
    val sectorId: Long,
    val fechaSiembra: String?,
    val fechaTrasplante: String?,
    val fechaEstimadaCosecha: String?,
    val frecuenciaRiegoDias: Int,
    val estado: String,
    val observaciones: String,
    val fotografiaUri: String?
)

data class TareaDto(
    val id: Long,
    val tipo: String,
    val descripcion: String,
    val sectorId: Long?,
    val cultivoId: Long?,
    val fechaProgramada: String,
    val fechaRealizacion: String?,
    val estudianteId: Long,
    val docenteCreadorId: Long,
    val estado: String,
    val comentarioEstudiante: String?,
    val comentarioDocente: String?,
    val fotografiaEvidenciaUri: String?,
    val fechaValidacion: String?,
    val docenteValidadorId: Long?
)

data class TareaRequestDto(
    val tipo: String,
    val descripcion: String,
    val sectorId: Long?,
    val cultivoId: Long?,
    val fechaProgramada: String,
    val fechaRealizacion: String?,
    val estudianteId: Long?,
    val docenteCreadorId: Long,
    val estado: String,
    val comentarioEstudiante: String?,
    val comentarioDocente: String?,
    val fotografiaEvidenciaUri: String?,
    val fechaValidacion: String?,
    val docenteValidadorId: Long?
)

data class TareaAvanceRequestDto(
    val estado: String,
    val comentarioEstudiante: String?,
    val fotografiaEvidenciaUri: String?
)

data class ReporteProblemaDto(
    val id: Long,
    val sectorId: Long?,
    val cultivoId: Long?,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: String,
    val usuarioReportadorId: Long,
    val fotografiaUri: String?,
    val estado: String,
    val comentarioDocente: String?,
    val fechaAtencion: String?
)

data class ReporteProblemaCreateRequestDto(
    val sectorId: Long?,
    val cultivoId: Long?,
    val tipo: String,
    val descripcion: String,
    val fotografiaUri: String?
)

data class ReporteProblemaUpdateRequestDto(
    val sectorId: Long?,
    val cultivoId: Long?,
    val tipo: String,
    val descripcion: String,
    val fechaReporte: String,
    val usuarioReportadorId: Long,
    val fotografiaUri: String?,
    val estado: String,
    val comentarioDocente: String?,
    val fechaAtencion: String?
)
