package com.jb.voyageur.feature.archetype

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors

@Composable
fun ArchetypeScreen(
    voyageurId: Long,
    onNaviguerVers: (EcranCreation) -> Unit,
    viewModel: ArchetypeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParcheminBackground {
        when (val state = uiState) {
            ArchetypeUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
                }
            }
            is ArchetypeUiState.Success -> {
                ArchetypeContent(
                    uiState             = state,
                    onNaviguerVers      = onNaviguerVers,
                    onNiveauSelectionne = viewModel::onNiveauSelectionne,
                    onCompetenceTappee  = viewModel::onCompetenceTappee
                )
            }
        }
    }
}

@Composable
fun ArchetypeContent(
    uiState: ArchetypeUiState.Success,
    onNaviguerVers: (EcranCreation) -> Unit,
    onNiveauSelectionne: (Int) -> Unit,
    onCompetenceTappee: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        BarreNavigationEcran(
            titre          = stringResource(R.string.menu_archetype),
            ecranCourant   = EcranCreation.ARCHETYPE,
            afficherSorts  = uiState.aDesSortsAccessibles,
            onNaviguerVers = onNaviguerVers
        )

        Row(modifier = Modifier.fillMaxSize()) {

            // ── Colonne gauche fixe 25% ──────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .padding(top = 8.dp, start = 8.dp, end = 4.dp)
            ) {
                // En-têtes
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = stringResource(R.string.archetype_niveau),
                        fontFamily = FontFamily.Serif,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (uiState.estComplet) VoyageurColors.NomCaracteristique.copy(alpha = 0.3f) else VoyageurColors.NomCaracteristique
                    )
                    Text(
                        text       = stringResource(R.string.archetype_nb),
                        fontFamily = FontFamily.Serif,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (uiState.estComplet) VoyageurColors.NomCaracteristique.copy(alpha = 0.3f) else VoyageurColors.NomCaracteristique,
                        modifier   = Modifier.padding(end = 12.dp)
                    )
                }
                HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.3f))

                // Lignes de niveaux +11 à +1
                uiState.colonneGauche.forEach { item ->
                    NiveauRow(
                        item    = item,
                        estComplet = uiState.estComplet,
                        onClick = {
                            if (!uiState.estComplet && item.restant > 0) onNiveauSelectionne(item.niveau)
                        }
                    )
                }
            }

            // Séparateur vertical
            VerticalDivider(
                color = VoyageurColors.NomCaracteristique.copy(alpha = 0.2f)
            )

            // ── Colonne droite scrollable 75% ────────────────────────
            LazyColumn(
                modifier       = Modifier
                    .weight(0.75f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = 8.dp, end = 8.dp,
                    top = 8.dp, bottom = 16.dp
                )
            ) {
                uiState.colonneDroite.forEach { categorie ->
                    // Titre de catégorie
                    item(key = "titre_${categorie.titre}") {
                        Text(
                            text       = stringResource(categorie.titre),
                            fontFamily = Luminari,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = VoyageurColors.NomCaracteristique,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp)
                        )
                        HorizontalDivider(
                            color = VoyageurColors.NomCaracteristique.copy(alpha = 0.2f)
                        )
                    }

                    // Compétences
                    items(
                        items = categorie.competences,
                        key   = { it.key }
                    ) { comp ->
                        CompetenceArchetypeRow(
                            comp       = comp,
                            estComplet = uiState.estComplet,
                            onClick    = { onCompetenceTappee(comp.key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NiveauRow(item: NiveauItem, estComplet: Boolean, onClick: () -> Unit) {
    val epuise = item.restant == 0 || estComplet

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !epuise, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            // Chevron ▶ devant le niveau sélectionné
            text = buildString {
                if (item.selectionne) append("▶ ")
                append("+${item.niveau}")
            },
            fontFamily = FontFamily.Serif,
            fontSize   = 14.sp,
            fontWeight = if (item.selectionne) FontWeight.Bold else FontWeight.Normal,
            color      = when {
                item.selectionne -> Color(0xFFFF0000)  // rouge vif
                epuise           -> VoyageurColors.NomCaracteristique.copy(alpha = 0.3f)
                else             -> VoyageurColors.NomCaracteristique
            }
        )
        Text(
            text       = item.restant.toString(),
            fontFamily = FontFamily.Serif,
            fontSize   = 14.sp,
            fontWeight = if (item.selectionne) FontWeight.Bold else FontWeight.Normal,
            color      = when {
                item.selectionne -> Color(0xFFFF0000)
                epuise           -> VoyageurColors.NomCaracteristique.copy(alpha = 0.3f)
                else             -> VoyageurColors.NomCaracteristique
            },
            modifier   = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
fun CompetenceArchetypeRow(comp: CompetenceArchetype, estComplet: Boolean, onClick: () -> Unit) {
    val niveauStr = when (comp.niveau) {
        null -> "-"
        0    -> "0"
        else -> "+${comp.niveau}"
    }

    val isClickable = !comp.estGrise && comp.niveau != 0 && (comp.niveau != null || !estComplet)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = comp.nom,
            fontFamily = FontFamily.Serif,
            fontSize   = 13.sp,
            color      = if (comp.estGrise) VoyageurColors.NomCaracteristique.copy(alpha = 0.2f) else VoyageurColors.NomCaracteristique,
            modifier   = Modifier.weight(1f)
        )
        Text(
            text       = niveauStr,
            fontFamily = FontFamily.Serif,
            fontSize   = 13.sp,
            fontWeight = if (comp.niveau != null) FontWeight.Bold else FontWeight.Normal,
            color      = when {
                comp.estGrise -> VoyageurColors.NomCaracteristique.copy(alpha = 0.2f)
                comp.niveau == null -> VoyageurColors.NomCaracteristique.copy(alpha = 0.4f)
                else                -> VoyageurColors.ValeurCaracteristique
            },
            modifier   = Modifier.padding(start = 8.dp, end = 32.dp)
        )
    }
}
