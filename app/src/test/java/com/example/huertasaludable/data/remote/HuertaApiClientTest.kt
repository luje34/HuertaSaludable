package com.example.huertasaludable.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HuertaApiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var api: HuertaApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenStore = FakeTokenStore()
        api = HuertaApiClient(server.url("/").toString(), tokenStore)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginSendsCredentialsWithoutBearerAndReadsTokenResponse() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "accessToken": "token-from-api",
                  "tokenType": "Bearer",
                  "expiresIn": 3600,
                  "usuario": {
                    "id": 9,
                    "nombre": "Camila",
                    "email": "camila@example.test",
                    "rol": "COLABORADOR",
                    "tallerGrupo": "Taller A",
                    "activo": true
                  }
                }
                """.trimIndent()
            )
        )

        val response = api.login("camila@example.test", "secret value")
        val request = server.takeRequest()

        assertEquals("/api/auth/login", request.path)
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("\"password\":\"secret value\""))
        assertEquals("token-from-api", response.accessToken)
        assertEquals("COLABORADOR", response.usuario.rol)
    }

    @Test
    fun protectedRequestsSendBearerTokenAndClearItAfterUnauthorizedResponse() = runBlocking {
        tokenStore.token = "session-token"
        server.enqueue(
            MockResponse().setResponseCode(401).setBody("""{"message":"Token expirado"}""")
        )

        val error = try {
            api.profile()
            null
        } catch (exception: ApiException) {
            exception
        }
        val request = server.takeRequest()

        assertEquals("Bearer session-token", request.getHeader("Authorization"))
        assertNotNull(error)
        assertEquals(401, error!!.statusCode)
        assertEquals("Token expirado", error.message)
        assertNull(tokenStore.token)
    }

    @Test
    fun registerSerializesWorkshopAndDoesNotAllowClientToSetRole() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "accessToken": "registered-token",
                  "tokenType": "Bearer",
                  "expiresIn": 3600,
                  "usuario": {
                    "id": 12,
                    "nombre": "Alex",
                    "email": "alex@example.test",
                    "rol": "COLABORADOR",
                    "tallerGrupo": "Taller B",
                    "activo": true
                  }
                }
                """.trimIndent()
            )
        )

        val response = api.register("Alex", "alex@example.test", "secure pass", "Taller B")
        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertEquals("/api/auth/register", request.path)
        assertTrue(body.contains("\"tallerGrupo\":\"Taller B\""))
        assertFalse(body.contains("\"rol\""))
        assertEquals("registered-token", response.accessToken)
    }

    private class FakeTokenStore : AccessTokenStore {
        var token: String? = null

        override fun save(token: String) {
            this.token = token
        }

        override fun read(): String? = token

        override fun clear() {
            token = null
        }
    }
}
