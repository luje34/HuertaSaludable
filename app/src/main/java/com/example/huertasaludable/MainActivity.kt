package com.example.huertasaludable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.huertasaludable.data.local.AppDatabase
import com.example.huertasaludable.BuildConfig
import com.example.huertasaludable.data.remote.HuertaApiClient
import com.example.huertasaludable.data.remote.SecureTokenStore
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.ui.navigation.NavGraph
import com.example.huertasaludable.ui.theme.HuertaSaludableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val tokenStore = SecureTokenStore(applicationContext)
        val repository = HuertaRepository(
            db = db,
            api = HuertaApiClient(BuildConfig.HUERTA_API_BASE_URL, tokenStore),
            tokenStore = tokenStore
        )

        setContent {
            HuertaSaludableTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    repository = repository
                )
            }
        }
    }
}
