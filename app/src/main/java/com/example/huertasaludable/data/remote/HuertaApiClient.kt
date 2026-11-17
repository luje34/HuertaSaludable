package com.example.huertasaludable.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(val statusCode: Int, message: String) : IOException(message)

class HuertaApiClient(
    baseUrl: String,
    private val tokenStore: AccessTokenStore,
    private val gson: Gson = Gson(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val baseUrl = baseUrl.trimEnd('/') + "/"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    init {
        require(this.baseUrl.startsWith("http://") || this.baseUrl.startsWith("https://")) {
            "La URL de la API debe comenzar con http:// o https://"
        }
    }

    suspend fun login(email: String, password: String): AuthResponseDto =
        gson.fromJson(
            request("POST", "api/auth/login", LoginRequestDto(email, password), authenticated = false),
            AuthResponseDto::class.java
        )

    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        tallerGrupo: String
    ): AuthResponseDto = gson.fromJson(
        request(
            "POST",
            "api/auth/register",
            RegisterRequestDto(nombre, email, password, tallerGrupo),
            authenticated = false
        ),
        AuthResponseDto::class.java
    )

    suspend fun profile(): UserDto = get("api/usuarios/me")
    suspend fun usuarios(): List<UserDto> = getList("api/usuarios")
    suspend fun sectores(): List<SectorDto> = getList("api/sectores")
    suspend fun cultivos(): List<CultivoDto> = getList("api/cultivos")
    suspend fun tareas(): List<TareaDto> = getList("api/tareas")
    suspend fun reportesProblemas(): List<ReporteProblemaDto> = getList("api/reportes-problemas")

    suspend fun crearSector(request: SectorRequestDto): SectorDto =
        post("api/sectores", request)

    suspend fun actualizarSector(id: Long, request: SectorRequestDto): SectorDto =
        put("api/sectores/$id", request)

    suspend fun eliminarSector(id: Long) {
        request("DELETE", "api/sectores/$id")
    }

    suspend fun crearCultivo(request: CultivoRequestDto): CultivoDto =
        post("api/cultivos", request)

    suspend fun actualizarCultivo(id: Long, request: CultivoRequestDto): CultivoDto =
        put("api/cultivos/$id", request)

    suspend fun eliminarCultivo(id: Long) {
        request("DELETE", "api/cultivos/$id")
    }

    suspend fun crearTarea(request: TareaRequestDto): TareaDto =
        post("api/tareas", request)

    suspend fun actualizarTarea(id: Long, request: TareaRequestDto): TareaDto =
        put("api/tareas/$id", request)

    suspend fun actualizarAvance(id: Long, request: TareaAvanceRequestDto): TareaDto =
        patch("api/tareas/$id/avance", request)

    suspend fun eliminarTarea(id: Long) {
        request("DELETE", "api/tareas/$id")
    }

    suspend fun crearReporte(request: ReporteProblemaCreateRequestDto): ReporteProblemaDto =
        post("api/reportes-problemas", request)

    suspend fun actualizarReporte(
        id: Long,
        request: ReporteProblemaUpdateRequestDto
    ): ReporteProblemaDto = put("api/reportes-problemas/$id", request)

    suspend fun eliminarReporte(id: Long) {
        request("DELETE", "api/reportes-problemas/$id")
    }

    private suspend inline fun <reified T> get(path: String): T =
        gson.fromJson(request("GET", path), T::class.java)

    private suspend inline fun <reified T> getList(path: String): List<T> =
        gson.fromJson(request("GET", path), object : TypeToken<List<T>>() {}.type)

    private suspend inline fun <reified T> post(path: String, body: Any): T =
        gson.fromJson(request("POST", path, body), T::class.java)

    private suspend inline fun <reified T> put(path: String, body: Any): T =
        gson.fromJson(request("PUT", path, body), T::class.java)

    private suspend inline fun <reified T> patch(path: String, body: Any): T =
        gson.fromJson(request("PATCH", path, body), T::class.java)

    private suspend fun request(
        method: String,
        path: String,
        body: Any? = null,
        authenticated: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url(baseUrl + path)
            .header("Accept", "application/json")

        if (authenticated) {
            tokenStore.read()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }
        val requestBody = body?.let { gson.toJson(it).toRequestBody(jsonMediaType) }
        val httpRequest = requestBuilder.method(
            method,
            requestBody ?: if (method == "GET" || method == "DELETE") null else EMPTY_REQUEST
        ).build()

        client.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                if (response.code == 401 && authenticated) tokenStore.clear()
                throw ApiException(response.code, apiErrorMessage(response.code, responseBody))
            }
            responseBody
        }
    }

    private fun apiErrorMessage(status: Int, responseBody: String): String {
        if (responseBody.isNotBlank()) {
            try {
                val json = JsonParser.parseString(responseBody)
                if (json.isJsonObject) {
                    val objectValue = json.asJsonObject
                    listOf("message", "detail", "error")
                        .firstNotNullOfOrNull { key ->
                            objectValue.get(key)
                                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                                ?.asString
                                ?.takeIf(String::isNotBlank)
                        }
                        ?.let { return it }
                }
            } catch (_: RuntimeException) {
                // Fall through to a generic status message for non-JSON error bodies.
            }
        }
        return "La API respondió con el error HTTP $status"
    }

    private companion object {
        val EMPTY_REQUEST = ByteArray(0).toRequestBody(null)
    }
}
