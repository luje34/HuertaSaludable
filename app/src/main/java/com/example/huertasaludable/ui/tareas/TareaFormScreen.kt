package com.example.huertasaludable.ui.tareas

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
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.data.model.TipoTarea
import com.example.huertasaludable.session.UserSessionManager
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.OutlinedButton


private fun aFechaPicker(fechaLocal: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = fechaLocal }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun desdeFechaPicker(fechaUtc: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = fechaUtc
    }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaFormScreen(
    tareaId: Long,
    viewModel: TareasViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    val formState by viewModel.formState.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()

    val estudiantes = usuarios.filter { it.rol == Rol.COLABORADOR }

    var tipoDropdownExpanded by remember { mutableStateOf(false) }
    var estudianteDropdownExpanded by remember { mutableStateOf(false) }
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var cultivoDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(tareaId) {
        viewModel.cargarTareaParaEditar(tareaId)
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
                        text = if (tareaId == 0L) "Asignar Nueva Tarea" else "Editar Tarea",
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
            ExposedDropdownMenuBox(
                expanded = tipoDropdownExpanded,
                onExpandedChange = { tipoDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.tipo.titulo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Tarea") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = tipoDropdownExpanded,
                    onDismissRequest = { tipoDropdownExpanded = false }
                ) {
                    TipoTarea.values().forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.titulo) },
                            onClick = {
                                viewModel.onTipoChanged(tipo)
                                tipoDropdownExpanded = false
                                
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { mostrarSelectorFecha = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val formato = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                Text("Fecha programada: ${formato.format(Date(formState.fechaProgramada))}")
            }

            if (mostrarSelectorFecha) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = aFechaPicker(formState.fechaProgramada)
                )

                DatePickerDialog(
                    onDismissRequest = { mostrarSelectorFecha = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { fecha ->
                                    viewModel.onFechaProgramadaChanged(desdeFechaPicker(fecha))
                                }
                                mostrarSelectorFecha = false
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarSelectorFecha = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = { Text("Instrucciones / Descripción") },
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
                expanded = estudianteDropdownExpanded,
                onExpandedChange = { estudianteDropdownExpanded = it }
            ) {
                val estudianteSeleccionado = estudiantes.find { it.id == formState.estudianteId }
                OutlinedTextField(
                    value = estudianteSeleccionado?.nombre ?: "Seleccionar Estudiante",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estudiante Responsable") },
                    isError = formState.estudianteError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estudianteDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = estudianteDropdownExpanded,
                    onDismissRequest = { estudianteDropdownExpanded = false }
                ) {
                    estudiantes.forEach { est ->
                        DropdownMenuItem(
                            text = { Text("${est.nombre} (${est.tallerGrupo})") },
                            onClick = {
                                viewModel.onEstudianteSelected(est.id)
                                estudianteDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (formState.estudianteError != null) {
                Text(
                    text = formState.estudianteError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = sectorDropdownExpanded,
                onExpandedChange = { sectorDropdownExpanded = it }
            ) {
                val sectorSeleccionado = sectores.find { it.id == formState.sectorId }
                OutlinedTextField(
                    value = sectorSeleccionado?.nombre ?: "Ninguno (Opcional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sector Relacionado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sectorDropdownExpanded,
                    onDismissRequest = { sectorDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno") },
                        onClick = {
                            viewModel.onSectorSelected(null)
                            sectorDropdownExpanded = false
                        }
                    )
                    sectores.forEach { sec ->
                        DropdownMenuItem(
                            text = { Text("${sec.codigo} • ${sec.nombre}") },
                            onClick = {
                                viewModel.onSectorSelected(sec.id)
                                sectorDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = cultivoDropdownExpanded,
                onExpandedChange = { cultivoDropdownExpanded = it }
            ) {
                val cultivoSeleccionado = cultivos.find { it.id == formState.cultivoId }
                OutlinedTextField(
                    value = cultivoSeleccionado?.nombre ?: "Ninguno (Opcional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cultivo Relacionado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cultivoDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = cultivoDropdownExpanded,
                    onDismissRequest = { cultivoDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno") },
                        onClick = {
                            viewModel.onCultivoSelected(null)
                            cultivoDropdownExpanded = false
                        }
                    )
                    cultivos.forEach { cul ->
                        DropdownMenuItem(
                            text = { Text("🌱 ${cul.nombre}") },
                            onClick = {
                                viewModel.onCultivoSelected(cul.id)
                                cultivoDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    currentUser?.id?.let { docenteId ->
                        viewModel.guardarTarea(docenteId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Asignar Tarea", fontWeight = FontWeight.Bold)
            }
        }
    }
}
