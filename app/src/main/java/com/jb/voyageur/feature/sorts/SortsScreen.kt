package com.jb.voyageur.feature.sorts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.Sort
import com.jb.voyageur.core.domain.model.VoieDraconic
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.launch

@Composable
fun SortsScreen(
    voyageurId: Long,
    onNaviguerVers: (EcranCreation) -> Unit,
    viewModel: SortsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageErreur by viewModel.messageErreur.collectAsStateWithLifecycle()

    ParcheminBackground {
        when (val state = uiState) {
            SortsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
                }
            }
            is SortsUiState.Success -> {
                SortsContent(
                    state = state,
                    onNaviguerVers = onNaviguerVers,
                    onAcheterSort = viewModel::acheterSort,
                    onRembourserSort = viewModel::rembourserSort
                )
            }
        }
    }

    messageErreur?.let { resId ->
        AlertDialog(
            onDismissRequest = viewModel::effacerErreur,
            title = { Text(stringResource(R.string.sorts_achat_impossible_titre)) },
            text = { Text(stringResource(resId)) },
            confirmButton = {
                TextButton(onClick = viewModel::effacerErreur) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SortsContent(
    state: SortsUiState.Success,
    onNaviguerVers: (EcranCreation) -> Unit,
    onAcheterSort: (Sort, Int) -> Unit,
    onRembourserSort: (Sort) -> Unit
) {
    val pagerState = rememberPagerState { state.colonnes.size }
    val scope = rememberCoroutineScope()
    var sortSelectionne by remember { mutableStateOf<Sort?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigationEcran(
                titre = stringResource(R.string.menu_sorts),
                ecranCourant = EcranCreation.SORTS,
                hautRevant = state.hautRevant,
                onNaviguerVers = onNaviguerVers
            )

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = VoyageurColors.NomCaracteristique,
                edgePadding = 8.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = if (state.colonnes[pagerState.currentPage].isConnu) Color.Black else VoyageurColors.NomCaracteristique
                        )
                    }
                }
            ) {
                state.colonnes.forEachIndexed { index, col ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = if (col.isConnu) stringResource(R.string.sorts_connu_titre) else col.titre,
                                fontFamily = Luminari,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (col.isConnu) Color.Black else VoyageurColors.NomCaracteristique
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).padding(top = 16.dp, bottom = 64.dp)
            ) { page ->
                val col = state.colonnes[page]
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (col.isConnu) {
                        val grouped = col.sorts.groupBy { it.voie }
                        grouped.forEach { (voie, sortsDansVoie) ->
                            item(key = "header_${voie.name}") {
                                Surface(
                                    color = VoyageurColors.NomCaracteristique.copy(alpha = 0.05f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = voie.name.lowercase().replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                                        fontFamily = Luminari,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                            items(sortsDansVoie, key = { it.nom + it.voie.name }) { sort ->
                                SpellRow(
                                    sort = sort,
                                    estAchete = true,
                                    niveauDraconic = state.niveauxDraconic[sort.voie] ?: -11,
                                    isConnuColumn = true,
                                    onClick = { sortSelectionne = sort }
                                )
                                HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f))
                            }
                        }
                    } else {
                        items(col.sorts, key = { it.nom + it.voie.name }) { sort ->
                            val estAchete = (sort.nom to sort.voie) in state.sortsAchetes
                            val niveauDraconic = state.niveauxDraconic[sort.voie] ?: -11
                            
                            SpellRow(
                                sort = sort,
                                estAchete = estAchete,
                                niveauDraconic = niveauDraconic,
                                isConnuColumn = false,
                                onClick = { sortSelectionne = sort }
                            )
                            HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f))
                        }
                    }
                }
            }
        }

        // Sticky footer for points
        Surface(
            color = VoyageurColors.ParcheminBase.copy(alpha = 0.3f),
            shadowElevation = 4.dp,
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(R.string.sorts_points_sorts, state.pointsSortsUtilises),
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoyageurColors.NomCaracteristique,
                    textAlign = TextAlign.End
                )
                Text(
                    text = stringResource(R.string.sorts_points_restants, state.pointsRestantsGlobal),
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    color = if (state.pointsRestantsGlobal < 0) VoyageurColors.ValeurCaracteristique else Color.Black,
                    textAlign = TextAlign.End
                )
            }
        }
    }

    sortSelectionne?.let { sort ->
        val niveau = state.niveauxDraconic[sort.voie] ?: -11
        val estConnu = (sort.nom to sort.voie) in state.sortsAchetes
        
        AchatSortDialog(
            sort = sort,
            niveauDraconic = niveau,
            estConnu = estConnu,
            onDismiss = { sortSelectionne = null },
            onAcheter = { onAcheterSort(sort, it); sortSelectionne = null },
            onRembourser = { onRembourserSort(sort); sortSelectionne = null }
        )
    }
}

@Composable
fun SpellRow(
    sort: Sort,
    estAchete: Boolean,
    niveauDraconic: Int,
    isConnuColumn: Boolean,
    onClick: () -> Unit
) {
    val accessible = sort.estAccessible(niveauDraconic)
    Text(
        text = sort.description,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        fontFamily = FontFamily.Serif,
        fontSize = 16.sp,
        color = when {
            isConnuColumn || estAchete -> VoyageurColors.NomCaracteristique // Bleu
            !accessible -> VoyageurColors.ValeurCaracteristique // Rouge
            else -> Color.Black
        }
    )
}

@Composable
fun AchatSortDialog(
    sort: Sort,
    niveauDraconic: Int,
    estConnu: Boolean,
    onDismiss: () -> Unit,
    onAcheter: (Int) -> Unit,
    onRembourser: () -> Unit
) {
    val accessible = sort.estAccessible(niveauDraconic)
    val coutBase = sort.calculerCoutDeBase()
    val supplement = sort.calculerSupplement(niveauDraconic)
    val coutTotal = coutBase + supplement
    val diffStr = sort.difficulte?.let { "R-$it" } ?: "variable"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "${sort.nom} $diffStr", 
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                if (estConnu) {
                    Text(stringResource(R.string.sorts_deja_connu))
                } else if (!accessible) {
                    Text(stringResource(R.string.sorts_inaccessible_niveau, sort.voie.name.lowercase()))
                } else {
                    Text(stringResource(R.string.sorts_cout, coutBase))
                    if (supplement > 0) {
                        Text(stringResource(R.string.sorts_supplement, supplement))
                        Text(stringResource(R.string.sorts_total, coutTotal), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (estConnu) {
                Button(onClick = onRembourser) { Text(stringResource(R.string.sorts_rembourser)) }
            } else if (accessible) {
                Button(onClick = { onAcheter(coutTotal) }) { Text(stringResource(R.string.sorts_acheter)) }
            } else {
                Button(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
            }
        },
        dismissButton = {
            if (estConnu || accessible) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.annuler)) }
            }
        }
    )
}
