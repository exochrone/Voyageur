package com.jb.voyageur.feature.competences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.FamilleCompetence
import com.jb.voyageur.core.domain.model.VoieDraconic
import com.jb.voyageur.core.ui.composable.AideBottomSheet
import com.jb.voyageur.core.ui.composable.CompetenceRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.launch

@Composable
fun CompetencesScreen(
    voyageurId: Long,
    onOpenDrawer: () -> Unit,
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
                    onAideRequise = viewModel::onDemanderAide,
                    onCompetenceChange = viewModel::onCompetenceChange,
                    onTroncChange = viewModel::onTroncChange,
                    onDraconicChange = viewModel::onDraconicChange
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
    onAideRequise: (String) -> Unit,
    onCompetenceChange: (String, Int, Int) -> Unit,
    onTroncChange: (String, String, Int) -> Unit,
    onDraconicChange: (VoieDraconic, Int) -> Unit
) {
    val pagerState = rememberPagerState { uiState.colonnes.size }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Indicateur de famille TabRow
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                edgePadding = 8.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = VoyageurColors.NomCaracteristique
                        )
                    }
                }
            ) {
                uiState.colonnes.forEachIndexed { index, colonne ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = stringResource(colonne.famille.titreColonneRes),
                                fontFamily = GoudyAcc,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Pager des colonnes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 24.dp)
                    .padding(bottom = 40.dp), // Réserver l'espace pour le sticky bottom
                verticalAlignment = Alignment.Top
            ) { page ->
                val colonne = uiState.colonnes[page]
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(colonne.items, key = { it.competence.nom }) { item ->
                        CompetenceRow(
                            nom = item.competence.nom,
                            niveauActuel = item.niveauActuel,
                            coutCumule = item.coutCumule,
                            borneInf = item.borneInf,
                            borneSup = item.borneSup,
                            onNiveauChange = { nouveau ->
                                when {
                                    item.appartientAuTronc != null ->
                                        onTroncChange(item.appartientAuTronc, item.competence.nom, nouveau)
                                    item.competence.famille == FamilleCompetence.DRACONIC -> {
                                        val voie = VoieDraconic.entries.firstOrNull {
                                            it.name.lowercase().replaceFirstChar { c -> c.uppercase() } == item.competence.nom
                                        }
                                        if (voie != null) onDraconicChange(voie, nouveau)
                                    }
                                    else ->
                                        onCompetenceChange(item.competence.nom, item.borneInf, nouveau)
                                }
                            },
                            onAideRequise = { onAideRequise(item.competence.nom) }
                        )
                        HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f))
                    }
                }
            }
        }

        // Compteur de points restants — Sticky Bottom
        Surface(
            color = VoyageurColors.ParcheminBase.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            val fullText = stringResource(R.string.xp_restants, uiState.pointsRestants)
            val valueStr = uiState.pointsRestants.toString()
            val startIndex = fullText.indexOf(valueStr)
            
            val annotatedText = buildAnnotatedString {
                append(fullText)
                if (startIndex != -1) {
                    addStyle(
                        style = SpanStyle(
                            color = if (uiState.pointsRestants < 0)
                                VoyageurColors.ValeurCaracteristique
                            else
                                Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        start = startIndex,
                        end = startIndex + valueStr.length
                    )
                }
            }

            Text(
                text = annotatedText,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                color = VoyageurColors.NomCaracteristique,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}
