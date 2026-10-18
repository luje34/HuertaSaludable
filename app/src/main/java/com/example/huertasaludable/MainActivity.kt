package com.example.huertasaludable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.huertasaludable.data.local.AppDatabase
import com.example.huertasaludable.data.local.DatabaseSeeder
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.ui.navigation.NavGraph
import com.example.huertasaludable.ui.theme.HuertaSaludableTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = HuertaRepository(db)

        // Precargar datos de prueba (4 usuarios, sectores, cultivos, tareas)
        lifecycleScope.launch {
            DatabaseSeeder.seed(db)
        }

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
