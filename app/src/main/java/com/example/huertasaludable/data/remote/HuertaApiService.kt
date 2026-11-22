package com.example.huertasaludable.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interfaz Retrofit para interactuar con la API REST de Huerta Saludable.
 */
interface HuertaApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @GET("api/usuarios/me")
    suspend fun profile(@Header("Authorization") token: String): Response<UserDto>

    @GET("api/usuarios")
    suspend fun getUsuarios(@Header("Authorization") token: String): Response<List<UserDto>>

    @GET("api/sectores")
    suspend fun getSectores(@Header("Authorization") token: String): Response<List<SectorDto>>

    @POST("api/sectores")
    suspend fun crearSector(
        @Header("Authorization") token: String,
        @Body request: SectorRequestDto
    ): Response<SectorDto>

    @PUT("api/sectores/{id}")
    suspend fun actualizarSector(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: SectorRequestDto
    ): Response<SectorDto>

    @DELETE("api/sectores/{id}")
    suspend fun eliminarSector(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/cultivos")
    suspend fun getCultivos(@Header("Authorization") token: String): Response<List<CultivoDto>>

    @POST("api/cultivos")
    suspend fun crearCultivo(
        @Header("Authorization") token: String,
        @Body request: CultivoRequestDto
    ): Response<CultivoDto>

    @PUT("api/cultivos/{id}")
    suspend fun actualizarCultivo(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: CultivoRequestDto
    ): Response<CultivoDto>

    @DELETE("api/cultivos/{id}")
    suspend fun eliminarCultivo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/tareas")
    suspend fun getTareas(@Header("Authorization") token: String): Response<List<TareaDto>>

    @POST("api/tareas")
    suspend fun crearTarea(
        @Header("Authorization") token: String,
        @Body request: TareaRequestDto
    ): Response<TareaDto>

    @PUT("api/tareas/{id}")
    suspend fun actualizarTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: TareaRequestDto
    ): Response<TareaDto>

    @DELETE("api/tareas/{id}")
    suspend fun eliminarTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/reportes-problemas")
    suspend fun getReportesProblemas(@Header("Authorization") token: String): Response<List<ReporteProblemaDto>>

    @POST("api/reportes-problemas")
    suspend fun crearReporteProblema(
        @Header("Authorization") token: String,
        @Body request: ReporteProblemaCreateRequestDto
    ): Response<ReporteProblemaDto>

    @PUT("api/reportes-problemas/{id}")
    suspend fun actualizarReporteProblema(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: ReporteProblemaUpdateRequestDto
    ): Response<ReporteProblemaDto>
}
