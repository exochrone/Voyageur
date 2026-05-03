package com.jb.voyageur.feature.competences

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.VoieDraconic
import com.jb.voyageur.core.ui.composable.AideBottomSheet
import com.jb.voyageur.core.ui.composable.CompetenceRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.composable.TroncRow
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetencesScreen(
    voyageurId: Long,
    onOpenDrawer: () -> Unit,
    viewModel: CompetencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding().height(48.dp),
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.menu_competences),
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            CompetencesContent(
                uiState = uiState,
                aideActive = aideActive,
                onCompetenceChange = viewModel::onCompetenceChange,
                onNiveauCommunChange = viewModel::onNiveauCommunChange,
                onMembreTroncChange = viewModel::onMembreTroncChange,
                onDraconicChange = viewModel::onDraconicChange,
                onDemanderAide = viewModel::onDemanderAide,
                onFermerAide = viewModel::onFermerAide
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompetencesContent(
    uiState: CompetencesUiState,
    aideActive: String?,
    onCompetenceChange: (String, Int, Int) -> Unit,
    onNiveauCommunChange: (String, Int) -> Unit,
    onMembreTroncChange: (String, String, Int) -> Unit,
    onDraconicChange: (VoieDraconic, Int) -> Unit,
    onDemanderAide: (String) -> Unit,
    onFermerAide: () -> Unit
) {
    ParcheminBackground {
        when (uiState) {
            CompetencesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
                }
            }
            is CompetencesUiState.Success -> {
                // ... pager logic ...
                val pagerState = rememberPagerState { uiState.colonnes.size }
                val scope = rememberCoroutineScope()

                Column(Modifier.fillMaxSize()) {
                    // Sticky Header XP
                    Surface(
                        color = VoyageurColors.ParcheminBase.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.xp_restants, uiState.pointsRestants),
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            color = if (uiState.pointsRestants < 0) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Familles TabRow (PrimaryScrollableTabRow)
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
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
                                        text = stringResource(colonne.famille.labelRes),
                                        fontFamily = GoudyAcc,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }

                    // Pager Colonnes
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val colonne = uiState.colonnes[page]
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(colonne.items, key = { item ->
                                when (item) {
                                    is CompetenceUiItem.Individuelle -> "ind_${item.competence.nom}"
                                    is CompetenceUiItem.TroncCommun -> "tronc_${item.tronc.nom}"
                                }
                            }) { item ->
                                when (item) {
                                    is CompetenceUiItem.Individuelle -> {
                                        CompetenceRow(
                                            nom = item.competence.nom,
                                            niveauActuel = item.niveauActuel,
                                            niveauBase = item.competence.niveauBase,
                                            coutCumule = item.coutCumule,
                                            estBloquee = item.estBloquee,
                                            onNiveauChange = { nouveau ->
                                                if (colonne.famille == com.jb.voyageur.core.domain.model.FamilleCompetence.DRACONIC) {
                                                    onDraconicChange(VoieDraconic.valueOf(item.competence.nom.uppercase()), nouveau)
                                                } else {
                                                    onCompetenceChange(item.competence.nom, item.competence.niveauBase, nouveau)
                                                }
                                            },
                                            onAideRequise = { onDemanderAide(item.competence.nom) }
                                        )
                                    }
                                    is CompetenceUiItem.TroncCommun -> {
                                        TroncRow(
                                            tronc = item.tronc,
                                            label = item.label,
                                            onNiveauCommunChange = { onNiveauCommunChange(item.tronc.nom, it) },
                                            onMembreChange = { membre, niveau -> onMembreTroncChange(item.tronc.nom, membre, niveau) },
                                            onAideRequise = onDemanderAide
                                        )
                                    }
                                }
                                HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
                
                aideActive?.let { nom ->
                    val context = LocalContext.current
                    val aide = remember(nom) { AideCompetenceProvider.pour(nom, context.resources) }
                    AideBottomSheet(
                        titre = aide.titre,
                        description = aide.description,
                        onDismiss = onFermerAide
                    )
                }
            }
        }
    }
}
