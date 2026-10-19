package com.example.huertasaludable.ui.tareas

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
fun TareaDeliveryScreen(
    tareaId: Long,
    viewModel: TareasViewModel,
    onDeliverySuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val deliveryState by viewModel.deliveryState.collectAsState()
    val tareas by viewModel.tareas.collectAsState()
    val currentUser by UserSessionManager.currentUser.collectAsState()

    val tarea = tareas.find { it.id == tareaId }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.onFotografiaEvidenciaSelected(selectedUri.toString())
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
            val photoFile = File(directory, "tarea_${tareaId}_${System.currentTimeMillis()}.jpg")

            try {
                if (!directory.exists() && !directory.mkdirs()) {
                    throw IOException("No se pudo crear la carpeta de evidencias")
                }

                FileOutputStream(photoFile).use { output ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                        throw IOException("No se pudo guardar la imagen")
                    }
                }

                viewModel.onFotografiaEvidenciaSelected(
                    android.net.Uri.fromFile(photoFile).toString()
                )
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entregar Trabajo", fontWeight = FontWeight.Bold) },
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
            if (tarea != null) {
                Text(
                    text = "Tarea: ${tarea.tipo.titulo}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = deliveryState.comentarioEstudiante,
                onValueChange = { viewModel.onComentarioEstudianteChanged(it) },
                label = { Text("Comentario / Observaciones del trabajo realizado") },
                isError = deliveryState.comentarioError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (deliveryState.comentarioError != null) {
                Text(
                    text = deliveryState.comentarioError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Fotografía de Evidencia:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = !deliveryState.fotografiaEvidenciaUri.isNullOrEmpty()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = Uri.parse(deliveryState.fotografiaEvidenciaUri),
                        contentDescription = "Evidencia fotográfica",
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
                Text("Adjuntar Foto de Evidencia (Photo Picker)")
            }
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar foto con la cámara")
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (tarea != null && currentUser != null) {
                        viewModel.entregarTarea(tarea, currentUser!!.id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar para Revisión", fontWeight = FontWeight.Bold)
            }
        }
    }
}
