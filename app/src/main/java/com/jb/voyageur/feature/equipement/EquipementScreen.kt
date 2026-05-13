package com.jb.voyageur.feature.equipement

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
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.launch

@Composable
fun EquipementScreen(
    voyageurId: Long,
    onNaviguerVers: (EcranCreation) -> Unit,
    viewModel: EquipementViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val messageErreur by viewModel.messageErreur.collectAsStateWithLifecycle()

    ParcheminBackground {
        when (val state = uiState) {
            EquipementUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
                }
            }
            is EquipementUiState.Success -> {
                EquipementContent(
                    uiState        = state,
                    onNaviguerVers = onNaviguerVers,
                    onAcheter      = viewModel::onAcheter,
                    onRembourser   = viewModel::onRembourser
                )
            }
        }
    }

    messageErreur?.let { resId ->
        AlertDialog(
            onDismissRequest = viewModel::effacerErreur,
            title = { Text(stringResource(R.string.equipement_achat_impossible_titre)) },
            text  = { Text(stringResource(resId)) },
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
fun EquipementContent(
    uiState: EquipementUiState.Success,
    onNaviguerVers: (EcranCreation) -> Unit,
    onAcheter: (ObjetEquipement) -> Unit,
    onRembourser: (String) -> Unit
) {
    // Colonne 0 = Possédés, colonnes 1..N = catalogue
    val nbColonnes = 1 + uiState.colonnesCatalogue.size
    val pagerState = rememberPagerState { nbColonnes }
    val scope      = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            BarreNavigationEcran(
                titre          = stringResource(R.string.menu_equipement),
                ecranCourant   = EcranCreation.EQUIPEMENT,
                hautRevant     = uiState.hautRevant,
                onNaviguerVers = onNaviguerVers
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor   = Color.Transparent,
                edgePadding      = 8.dp,
                divider          = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = VoyageurColors.NomCaracteristique
                        )
                    }
                }
            ) {
                // Tab "Équipement possédé"
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick  = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                        Text(
                            text       = stringResource(R.string.equipement_possede_titre),
                            fontFamily = Luminari,
                            fontSize   = 18.sp,
                            color      = Color.Black  // libellé noir
                        )
                    }
                )
                // Tabs catégories
                uiState.colonnesCatalogue.forEachIndexed { index, col ->
                    Tab(
                        selected = pagerState.currentPage == index + 1,
                        onClick  = { scope.launch { pagerState.animateScrollToPage(index + 1) } },
                        text = {
                            Text(
                                text       = col.categorie.nom,
                                fontFamily = Luminari,
                                fontSize   = 18.sp,
                                color      = VoyageurColors.NomCaracteristique  // libellé bleu
                            )
                        }
                    )
                }
            }

            // Pager
            HorizontalPager(
                state             = pagerState,
                modifier          = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, bottom = 64.dp), // Ajusté pour le footer plus large
                verticalAlignment = Alignment.Top
            ) { page ->
                if (page == 0) {
                    ColonnePossedes(
                        colonne      = uiState.colonnePossedes,
                        onRembourser = onRembourser
                    )
                } else {
                    ColonneCatalogue(
                        colonne   = uiState.colonnesCatalogue[page - 1],
                        onAcheter = onAcheter
                    )
                }
            }
        }

        // Sticky bas — Fortune + Encombrement total
        Surface(
            color           = VoyageurColors.ParcheminBase.copy(alpha = 0.3f),
            shadowElevation = 4.dp,
            modifier        = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            Column(
                modifier             = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment  = Alignment.End
            ) {
                Text(
                    text       = stringResource(
                        R.string.equipement_fortune,
                        uiState.fortune
                    ),
                    fontFamily = FontFamily.Serif,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (uiState.fortune < 0)
                        VoyageurColors.ValeurCaracteristique
                    else
                        VoyageurColors.NomCaracteristique,
                    textAlign  = TextAlign.End
                )
                Text(
                    text       = stringResource(
                        R.string.equipement_enc_total,
                        uiState.colonnePossedes.encTotal,
                        uiState.encMax
                    ),
                    fontFamily = FontFamily.Serif,
                    fontSize   = 13.sp,
                    color      = if (uiState.colonnePossedes.encTotal > uiState.encMax)
                        VoyageurColors.ValeurCaracteristique
                    else
                        VoyageurColors.NomCaracteristique,
                    textAlign  = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun ColonnePossedes(
    colonne: ColonneEquipement.Possedes,
    onRembourser: (String) -> Unit
) {
    if (colonne.groupes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text      = stringResource(R.string.equipement_aucun_objet),
                fontFamily = FontFamily.Serif,
                color     = VoyageurColors.NomCaracteristique.copy(alpha = 0.5f)
            )
        }
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        colonne.groupes.forEach { (categorie, objets) ->
            // Sous-titre de catégorie
            item(key = "header_$categorie") {
                Surface(
                    color    = VoyageurColors.NomCaracteristique.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = categorie,
                        fontFamily = Luminari,
                        fontSize   = 16.sp,
                        color      = Color.Black,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            // Objets de la catégorie
            items(objets, key = { it.nom }) { objet ->
                ObjetPossedeRow(
                    objet        = objet,
                    onRembourser = { onRembourser(objet.nom) }
                )
                HorizontalDivider(
                    color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f)
                )
            }
        }
    }
}

@Composable
fun ObjetPossedeRow(
    objet: ObjetPossede,
    onRembourser: () -> Unit
) {
    // Tap → rembourser 1 unité
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRembourser)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = buildString {
                if (objet.quantite > 1) append("${objet.quantite} ")
                append(objet.nom)
            },
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = VoyageurColors.NomCaracteristique
        )
        Text(
            text = "Enc : ${formatEnc(objet.encombrementTotal)} - Prix : ${objet.prixTotal}d",
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = VoyageurColors.NomCaracteristique.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ColonneCatalogue(
    colonne: ColonneEquipement.Catalogue,
    onAcheter: (ObjetEquipement) -> Unit
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(colonne.categorie.objets, key = { it.nom }) { objet ->
            val quantite = colonne.quantitesAchetees[objet.nom] ?: 0
            ObjetCatalogueRow(
                objet      = objet,
                quantite   = quantite,
                onAcheter  = { onAcheter(objet) }
            )
            HorizontalDivider(
                color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
fun ObjetCatalogueRow(
    objet: ObjetEquipement,
    quantite: Int,
    onAcheter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAcheter)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = objet.nom,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VoyageurColors.NomCaracteristique,
                modifier = Modifier.weight(1f)
            )
            if (quantite > 0) {
                Text(
                    text = "x$quantite",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Text(
            text = "Enc : ${formatEnc(objet.encombrement)} - Prix : ${objet.prix}d",
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = VoyageurColors.NomCaracteristique.copy(alpha = 0.8f)
        )
    }
}

// Affiche 0 si encombrement nul, sinon le float sans zéro inutile
// Exemples : 0.0 → "0", 0.4 → "0,4", 1.0 → "1", 0.3 → "0,3"
fun formatEnc(enc: Float): String {
    return if (enc == 0f) "0"
    else "%.1f".format(java.util.Locale.FRANCE, enc).trimEnd('0').trimEnd(',')
}
