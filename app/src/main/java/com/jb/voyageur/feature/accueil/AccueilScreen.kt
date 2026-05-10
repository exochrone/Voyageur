package com.jb.voyageur.feature.accueil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.Luminari

@Composable
fun AccueilScreen(
    onNavigateToCaracteristiques: (Long) -> Unit,
    viewModel: AccueilViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is AccueilNavigation.VersCaracteristiques ->
                    onNavigateToCaracteristiques(event.voyageurId)
            }
        }
    }

    AccueilContent(
        onCreerVoyageur = viewModel::onCreerVoyageur
    )
}

@Composable
fun AccueilContent(
    onCreerVoyageur: () -> Unit
) {
    ParcheminBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontFamily = Luminari,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Creation de personnages\npour Rêve de Dragon",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onCreerVoyageur,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.accueil_creer_voyageur).uppercase(),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { },
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black.copy(alpha = 0.6f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(
                    text = "OUVRIR UNE FICHE DE VOYAGEUR",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccueilContentPreview() {
    AccueilContent(
        onCreerVoyageur = {}
    )
}
