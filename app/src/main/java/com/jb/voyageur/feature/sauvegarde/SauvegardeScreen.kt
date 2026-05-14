package com.jb.voyageur.feature.sauvegarde

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.main.MainViewModel
import com.jb.voyageur.feature.main.PdfExportState

@Composable
fun SauvegardeScreen(
    viewModel: MainViewModel
) {
    BackHandler(enabled = true) {
        // Retour système autorisé
    }
    val context = LocalContext.current
    val voyageurNom by viewModel.voyageurNom.collectAsStateWithLifecycle()
    val pdfExportState by viewModel.pdfExportState.collectAsStateWithLifecycle()

    // Launcher pour l'export JSON (.rdd)
    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.onSaveJson(context, uri)
        }
    }

    ParcheminBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.menu_sauvegarde),
                fontFamily = Luminari,
                fontSize = 32.sp,
                color = VoyageurColors.NomCaracteristique,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = { 
                    val fileName = if (voyageurNom.isBlank()) "voyageur.rdd" else "$voyageurNom.rdd"
                    createJsonLauncher.launch(fileName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(R.string.sauvegarde_bouton_json),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onExportPdf() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(R.string.sauvegarde_bouton_pdf),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            if (pdfExportState is PdfExportState.Loading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Color.Black)
            }
        }
    }
}
