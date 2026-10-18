package com.example.huertasaludable.ui.sectores

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertasaludable.data.local.entity.SectorEntity
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.session.UserSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectoresListScreen(
    viewModel: SectoresViewModel,
    onSectorClick: (Long) -> Unit,
    onAddSectorClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()
    val isAdmin = currentUser?.rol == Rol.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sectores del Huerto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddSectorClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Sector")
                }
            }
        }
    ) { innerPadding ->
        if (sectores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay sectores registrados aún.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sectores) { sector ->
                    val numCultivos = cultivos.count { it.sectorId == sector.id }
                    SectorCard(
                        sector = sector,
                        numCultivos = numCultivos,
                        onClick = { onSectorClick(sector.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectorCard(
    sector: SectorEntity,
    numCultivos: Int,
    onClick: () -> Unit
) {
    val badgeBg = when (sector.estado) {
        EstadoSector.DISPONIBLE -> MaterialTheme.colorScheme.primaryContainer
        EstadoSector.EN_USO -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoSector.EN_MANTENIMIENTO -> MaterialTheme.colorScheme.errorContainer
        EstadoSector.FUERA_DE_USO -> MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeTextColor = when (sector.estado) {
        EstadoSector.DISPONIBLE -> MaterialTheme.colorScheme.onPrimaryContainer
        EstadoSector.EN_USO -> MaterialTheme.colorScheme.onTertiaryContainer
        EstadoSector.EN_MANTENIMIENTO -> MaterialTheme.colorScheme.onErrorContainer
        EstadoSector.FUERA_DE_USO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sector.codigo} • ${sector.nombre}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sector.estado.titulo,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = sector.ubicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = sector.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "🌱 $numCultivos cultivo(s) asociados",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
