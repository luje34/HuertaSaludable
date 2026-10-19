package com.example.huertasaludable.ui.cultivos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.huertasaludable.data.model.EstadoCultivo
import com.example.huertasaludable.session.UserSessionManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoFormScreen(
    cultivoId: Long,
    sectorIdInicial: Long,
    viewModel: CultivosViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()

    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var estadoDropdownExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.onFotografiaSelected(selectedUri.toString())
            } catch (error: SecurityException) {
                Toast.makeText(
                    context,
                    "No se pudo conservar el acceso a la foto. Intenta elegirla otra vez.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val directory = File(context.filesDir, "evidencias")
            val photoFile = File(
                directory,
                "cultivo_${cultivoId}_${System.currentTimeMillis()}.jpg"
            )

            try {
                if (!directory.exists() && !directory.mkdirs()) {
                    throw IOException("No se pudo crear la carpeta de evidencias")
                }

                FileOutputStream(photoFile).use { output ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                        throw IOException("No se pudo guardar la imagen")
                    }
                }

                viewModel.onFotografiaSelected(Uri.fromFile(photoFile).toString())
            } catch (error: IOException) {
                photoFile.delete()
                Toast.makeText(
                    context,
                    "No se pudo guardar la foto: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    LaunchedEffect(cultivoId) {
        viewModel.cargarCultivoParaEditar(cultivoId, sectorIdInicial)
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
                        text = if (cultivoId == 0L) "Nuevo Cultivo" else "Editar Cultivo",
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
                value = formState.nombre,
                onValueChange = { viewModel.onNombreChanged(it) },
                label = { Text("Nombre del Cultivo (ej: Tomate Cherry)") },
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
                value = formState.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = { Text("Descripción") },
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
                expanded = sectorDropdownExpanded,
                onExpandedChange = { sectorDropdownExpanded = it }
            ) {
                val sectorSeleccionado = sectores.find { it.id == formState.sectorId }
                OutlinedTextField(
                    value = sectorSeleccionado?.nombre ?: "Seleccionar Sector",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sector Asignado") },
                    isError = formState.sectorError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sectorDropdownExpanded,
                    onDismissRequest = { sectorDropdownExpanded = false }
                ) {
                    sectores.forEach { sector ->
                        DropdownMenuItem(
                            text = { Text("${sector.codigo} • ${sector.nombre}") },
                            onClick = {
                                viewModel.onSectorSelected(sector.id)
                                sectorDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (formState.sectorError != null) {
                Text(
                    text = formState.sectorError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.frecuenciaRiegoDiasStr,
                onValueChange = { viewModel.onFrecuenciaRiegoChanged(it) },
                label = { Text("Frecuencia de Riego (días)") },
                isError = formState.riegoError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (formState.riegoError != null) {
                Text(
                    text = formState.riegoError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (formState.fechaError != null) {
                Text(
                    text = formState.fechaError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            ExposedDropdownMenuBox(
                expanded = estadoDropdownExpanded,
                onExpandedChange = { estadoDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.estado.titulo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado de Desarrollo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = estadoDropdownExpanded,
                    onDismissRequest = { estadoDropdownExpanded = false }
                ) {
                    EstadoCultivo.values().forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.titulo) },
                            onClick = {
                                viewModel.onEstadoChanged(estado)
                                estadoDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.observaciones,
                onValueChange = { viewModel.onObservacionesChanged(it) },
                label = { Text("Observaciones del Cultivo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fotografía Referencial:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = !formState.fotografiaUri.isNullOrEmpty()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = Uri.parse(formState.fotografiaUri),
                        contentDescription = "Vista previa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Galería (Photo Picker)", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cámara", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    currentUser?.id?.let { userId ->
                        viewModel.guardarCultivo(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cultivo", fontWeight = FontWeight.Bold)
            }
        }
    }
}
