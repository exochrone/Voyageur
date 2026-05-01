package com.jb.voyageur.feature.accueil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.GoudyAcc

@Composable
fun AccueilScreen(
    onNavigateToCaracteristiques: (Long) -> Unit,
    viewModel: AccueilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                is AccueilNavigation.VersCaracteristiques ->
                    onNavigateToCaracteristiques(event.voyageurId)
            }
        }
    }

    AccueilContent(
        uiState = uiState,
        onNomChange = viewModel::onNomChange,
        onSexeChange = viewModel::onSexeChange,
        onHautRevantChange = viewModel::onHautRevantChange,
        onCreerVoyageur = viewModel::onCreerVoyageur
    )
}

@Composable
fun AccueilContent(
    uiState: AccueilUiState,
    onNomChange: (String) -> Unit,
    onSexeChange: (Sexe) -> Unit,
    onHautRevantChange: (Boolean) -> Unit,
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
                fontFamily = GoudyAcc,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.accueil_sous_titre),
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(32.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.vrai_revant))
                Switch(
                    checked = uiState.hautRevant,
                    onCheckedChange = onHautRevantChange
                )
                Text(stringResource(R.string.haut_revant))
            }
            
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.nom,
                onValueChange = onNomChange,
                label = { Text(stringResource(R.string.accueil_nom_personnage)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onSexeChange(Sexe.HOMME) },
                    enabled = uiState.sexe != Sexe.HOMME
                ) {
                    Text(stringResource(R.string.homme))
                }
                Button(
                    onClick = { onSexeChange(Sexe.FEMME) },
                    enabled = uiState.sexe != Sexe.FEMME
                ) {
                    Text(stringResource(R.string.femme))
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Button(onClick = onCreerVoyageur) {
                Text(stringResource(R.string.accueil_creer_voyageur))
            }
        }
    }
}

@Preview
@Composable
private fun AccueilContentPreview() {
    AccueilContent(
        uiState = AccueilUiState(),
        onNomChange = {},
        onSexeChange = {},
        onHautRevantChange = {},
        onCreerVoyageur = {}
    )
}
