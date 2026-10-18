package com.example.huertasaludable.ui.problemas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.example.huertasaludable.data.model.TipoProblema
import com.example.huertasaludable.session.UserSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarProblemaScreen(
    viewModel: ProblemasViewModel,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val sectores by viewModel.sectores.collectAsState()
    val cultivos by viewModel.cultivos.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()

    var tipoDropdownExpanded by remember { mutableStateOf(false) }
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var cultivoDropdownExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFotografiaSelected(it.toString()) }
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Problema", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Tipo de Problema") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = tipoDropdownExpanded,
                    onDismissRequest = { tipoDropdownExpanded = false }
                ) {
                    TipoProblema.values().forEach { tipo ->
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

            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = { Text("Descripción del Problema") },
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fotografía Adjunta (Opcional):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!formState.fotografiaUri.isNullOrEmpty()) {
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

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Adjuntar Foto (Photo Picker)")
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    currentUser?.id?.let { userId ->
                        viewModel.guardarReporte(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar Reporte de Problema", fontWeight = FontWeight.Bold)
            }
        }
    }
}
