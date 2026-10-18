package com.example.huertasaludable.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertasaludable.session.UserSessionManager
import com.example.huertasaludable.ui.components.HuertaTopAppBar

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun StudentDashboardScreen(
    onNavigateToMisTareas: () -> Unit,
    onNavigateToReportarProblema: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by UserSessionManager.currentUser.collectAsState()

    Scaffold(
        topBar = {
            HuertaTopAppBar(
                title = "Panel del Estudiante",
                onLogoutClicked = onLogout
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "👩‍🎓 ¡Hola, ${currentUser?.nombre ?: "Estudiante"}!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grupo: ${currentUser?.tallerGrupo ?: "Taller Escolar"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            Text(
                text = "Tus Opciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Mis Tareas",
                    subtitle = "Ejecutar y enviar evidencia",
                    icon = "📋",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMisTareas
                )
                DashboardCard(
                    title = "Reportar Problema",
                    subtitle = "Avisar sobre plagas o agua",
                    icon = "⚠️",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReportarProblema
                )
            }
        }
    }
}
