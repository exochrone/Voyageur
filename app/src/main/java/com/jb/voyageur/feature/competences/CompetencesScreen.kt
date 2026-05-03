package com.jb.voyageur.feature.competences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.AideBottomSheet
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.competences.composable.DoubleListeCompetences

@Composable
fun CompetencesScreen(
    voyageurId: Long,
    viewModel: CompetencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()

    ParcheminBackground {
        when (val state = uiState) {
            CompetencesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
                }
            }
            is CompetencesUiState.Success -> {
                CompetencesContent(
                    uiState = state,
                    onAideRequise = viewModel::onDemanderAide
                )
            }
        }
    }

    // Aide contextuelle — bottom sheet
    aideActive?.let { nom ->
        val context = LocalContext.current
        val aide = remember(nom) { AideCompetenceProvider.pour(nom, context.resources) }
        AideBottomSheet(
            titre = aide.titre,
            description = aide.description,
            onDismiss = viewModel::onFermerAide
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompetencesContent(
    uiState: CompetencesUiState.Success,
    onAideRequise: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // Compteur XP sticky
        stickyHeader {
            Surface(
                color = VoyageurColors.ParcheminBase.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.xp_restants, uiState.pointsRestants),
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = if (uiState.pointsRestants < 0)
                        VoyageurColors.ValeurCaracteristique
                    else
                        VoyageurColors.NomCaracteristique,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Les 4 doubles listes
        items(uiState.doubleListes) { doubleListe ->
            DoubleListeCompetences(
                doubleListe = doubleListe,
                onAideRequise = onAideRequise,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            HorizontalDivider(
                color = VoyageurColors.NomCaracteristique.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
