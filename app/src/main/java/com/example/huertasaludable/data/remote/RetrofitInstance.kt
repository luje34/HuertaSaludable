package com.example.huertasaludable.data.remote

import com.example.huertasaludable.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Proveedor Singleton de la instancia Retrofit para la API de Huerta Saludable.
 */
object RetrofitInstance {

    fun create(baseUrl: String = BuildConfig.HUERTA_API_BASE_URL): HuertaApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuertaApiService::class.java)
    }
}
