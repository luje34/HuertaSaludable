package com.example.huertasaludable.ui.tareas

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertasaludable.data.local.entity.TareaEntity
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import com.example.huertasaludable.data.model.EstadoTarea
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.session.UserSessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasListScreen(
    viewModel: TareasViewModel,
    onTareaClick: (Long) -> Unit,
    onAddTareaClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()
    val isAdmin = currentUser?.rol == Rol.ADMIN

    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("Todas", "Pendientes", "Por Validar", "Validadas", "Rechazadas")

    val tareasFiltradas = remember(tareas, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> tareas.filter { it.estado == EstadoTarea.PENDIENTE || it.estado == EstadoTarea.EN_DESARROLLO }
            2 -> tareas.filter { it.estado == EstadoTarea.PENDIENTE_VALIDACION }
            3 -> tareas.filter { it.estado == EstadoTarea.VALIDADA }
            4 -> tareas.filter { it.estado == EstadoTarea.RECHAZADA }
            else -> tareas
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Gestión de Tareas", fontWeight = FontWeight.Bold) },
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
                ScrollableTabRow(selectedTabIndex = selectedFilterIndex) {
                    filters.forEachIndexed { index, text ->
                        Tab(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            text = { Text(text, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddTareaClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Asignar Tarea")
                }
            }
        }
    ) { innerPadding ->
        if (tareasFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay tareas en esta categoría.",
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
                items(tareasFiltradas) { tarea ->
                    val estudiante = usuarios.find { it.id == tarea.estudianteId }
                    TareaCard(
                        tarea = tarea,
                        estudiante = estudiante,
                        onClick = { onTareaClick(tarea.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: TareaEntity,
    estudiante: UsuarioEntity?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val badgeBg = when (tarea.estado) {
        EstadoTarea.PENDIENTE -> MaterialTheme.colorScheme.surfaceVariant
        EstadoTarea.EN_DESARROLLO -> MaterialTheme.colorScheme.primaryContainer
        EstadoTarea.PENDIENTE_VALIDACION -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoTarea.VALIDADA -> MaterialTheme.colorScheme.secondaryContainer
        EstadoTarea.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
        EstadoTarea.ATRASADA -> MaterialTheme.colorScheme.errorContainer
    }

    val badgeTextColor = when (tarea.estado) {
        EstadoTarea.PENDIENTE -> MaterialTheme.colorScheme.onSurfaceVariant
        EstadoTarea.EN_DESARROLLO -> MaterialTheme.colorScheme.onPrimaryContainer
        EstadoTarea.PENDIENTE_VALIDACION -> MaterialTheme.colorScheme.onTertiaryContainer
        EstadoTarea.VALIDADA -> MaterialTheme.colorScheme.onSecondaryContainer
        EstadoTarea.RECHAZADA -> MaterialTheme.colorScheme.onErrorContainer
        EstadoTarea.ATRASADA -> MaterialTheme.colorScheme.onErrorContainer
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
                    text = "📋 ${tarea.tipo.titulo}",
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
                        text = tarea.estado.titulo,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tarea.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👤 Asignado: ${estudiante?.nombre ?: "Sin asignar"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "📅 Programada: ${dateFormat.format(Date(tarea.fechaProgramada))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
