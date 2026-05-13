package com.jb.voyageur.feature.options

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OptionsScreen(
    viewModel: OptionsViewModel = hiltViewModel()
) {
    val backgroundImageUri by viewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.onBackgroundImageSelected(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Options",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Debug",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Image de fond globale",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (backgroundImageUri != null) "Image sélectionnée : $backgroundImageUri" else "Aucune image sélectionnée",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
                        Text("Choisir une image")
                    }
                    if (backgroundImageUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { viewModel.onBackgroundImageSelected(null) }) {
                            Text("Réinitialiser", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
