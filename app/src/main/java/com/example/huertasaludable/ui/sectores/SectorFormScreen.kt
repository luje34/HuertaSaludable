package com.example.huertasaludable.ui.sectores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertasaludable.data.model.EstadoSector
import com.example.huertasaludable.session.UserSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorFormScreen(
    sectorId: Long,
    viewModel: SectoresViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(sectorId) {
        viewModel.cargarSectorParaEditar(sectorId)
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (sectorId == 0L) "Nuevo Sector" else "Editar Sector",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = formState.codigo,
                onValueChange = { viewModel.onCodigoChanged(it) },
                label = { Text("Código (ej: SEC-01)") },
                isError = formState.codigoError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (formState.codigoError != null) {
                Text(
                    text = formState.codigoError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.nombre,
                onValueChange = { viewModel.onNombreChanged(it) },
                label = { Text("Nombre del Sector") },
                isError = formState.nombreError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (formState.nombreError != null) {
                Text(
                    text = formState.nombreError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.ubicacion,
                onValueChange = { viewModel.onUbicacionChanged(it) },
                label = { Text("Ubicación Física") },
                isError = formState.ubicacionError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (formState.ubicacionError != null) {
                Text(
                    text = formState.ubicacionError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = { Text("Descripción General") },
                isError = formState.descripcionError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (formState.descripcionError != null) {
                Text(
                    text = formState.descripcionError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.estado.titulo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado del Sector") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    EstadoSector.values().forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.titulo) },
                            onClick = {
                                viewModel.onEstadoChanged(estado)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    currentUser?.id?.let { userId ->
                        viewModel.guardarSector(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Sector", fontWeight = FontWeight.Bold)
            }
        }
    }
}
