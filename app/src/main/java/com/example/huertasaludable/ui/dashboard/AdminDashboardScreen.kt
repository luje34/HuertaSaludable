package com.example.huertasaludable.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertasaludable.session.UserSessionManager
import com.example.huertasaludable.ui.components.HuertaTopAppBar

@Composable
fun AdminDashboardScreen(
    onNavigateToSectores: () -> Unit,
    onNavigateToCultivos: () -> Unit,
    onNavigateToTareas: () -> Unit,
    onNavigateToProblemas: () -> Unit,
    onNavigateToSensores: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            HuertaTopAppBar(
                title = "Panel del Docente",
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "👨‍🏫 ¡Bienvenido, ${UserSessionManager.currentUser.value?.nombre ?: "Docente"}!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gestión integral del huerto escolar, sectores, cultivos y tareas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Módulos Principales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Sectores",
                    subtitle = "Crear/Editar sectores",
                    icon = "🏡",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSectores
                )
                DashboardCard(
                    title = "Cultivos",
                    subtitle = "Gestionar siembras",
                    icon = "🌱",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCultivos
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Tareas",
                    subtitle = "Asignar y validar",
                    icon = "📋",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTareas
                )
                DashboardCard(
                    title = "Problemas",
                    subtitle = "Revisar reportes",
                    icon = "⚠️",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProblemas
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Sensores",
                    subtitle = "Lecturas simuladas",
                    icon = "📊",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSensores
                )
                DashboardCard(
                    title = "Historial",
                    subtitle = "Registro de cambios",
                    icon = "📜",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistorial
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
