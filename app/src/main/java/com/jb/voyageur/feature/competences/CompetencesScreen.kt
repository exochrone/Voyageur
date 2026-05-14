package com.jb.voyageur.feature.competences

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.ui.composable.AideBottomSheet
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.composable.CompetenceRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.launch

@Composable
fun CompetencesScreen(
    voyageurId: Long,
    onNaviguerVers: (EcranCreation) -> Unit,
    viewModel: CompetencesViewModel = hiltViewModel()
) {
    BackHandler(enabled = true) {
        // Bloquer le retour système
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()
    val isXPBlocked by viewModel.isXPBlocked.collectAsStateWithLifecycle()
    val messageDraconicBloque by viewModel.messageDraconicBloque.collectAsStateWithLifecycle()
    var highlightedSkills by remember { mutableStateOf(setOf<String>()) }
    
    var showAddCustomDialog by remember { mutableStateOf<Pair<FamilleCompetence, Int>?>(null) }
    var showManageCustomDialog by remember { mutableStateOf<CompetenceUiItem.Individuelle?>(null) }

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
                    isXPBlocked = isXPBlocked,
                    highlightedSkills = highlightedSkills,
                    onNaviguerVers = onNaviguerVers,
                    onAideRequise = viewModel::onDemanderAide,
                    onCompetenceChange = viewModel::onCompetenceChange,
                    onTroncChange = viewModel::onTroncChange,
                    onDraconicChange = viewModel::onDraconicChange,
                    onDragEnd = {
                        viewModel.resetXPBlocked()
                        highlightedSkills = emptySet()
                    },
                    onAtBorneChange = { item, isAtBorne ->
                        if (isAtBorne) {
                            when {
                                item.competence.nom in CatalogueCompetences.SURVIES_SPECIFIQUES && 
                                    item.niveauActuel < 0 && 
                                    item.niveauActuel == item.borneSup -> {
                                    highlightedSkills = setOf("Survie en extérieur")
                                }
                                item.competence.nom == "Survie en extérieur" && 
                                    item.niveauActuel == item.borneInf &&
                                    item.borneInf > -8 -> { 
                                    // Trouver toutes les survies spécifiques
                                    val specificItems = state.colonnes
                                        .flatMap { it.items }
                                        .filterIsInstance<CompetenceUiItem.Individuelle>()
                                        .filter { it.competence.nom in CatalogueCompetences.SURVIES_SPECIFIQUES }
                                    
                                    val maxLevel = specificItems.maxOfOrNull { it.niveauActuel } ?: -8
                                    
                                    val blockers = specificItems
                                        .filter { it.niveauActuel == maxLevel }
                                        .map { it.competence.nom }
                                        
                                    highlightedSkills = blockers.toSet()
                                }
                                item.appartientAuTronc != null && item.niveauActuel == 0 && item.borneInf == 0 -> {
                                    // Bloqué à 0 car un autre membre du tronc est > 0
                                    val troncItems = state.colonnes
                                        .flatMap { it.items }
                                        .filterIsInstance<CompetenceUiItem.Individuelle>()
                                        .filter { it.appartientAuTronc == item.appartientAuTronc }
                                    
                                    val maxLevel = troncItems.maxOfOrNull { it.niveauActuel } ?: 0
                                    val blockers = troncItems
                                        .filter { it.niveauActuel == maxLevel && it.competence.nom != item.competence.nom }
                                        .map { it.competence.nom }
                                    
                                    highlightedSkills = blockers.toSet()
                                }
                            }
                        } else {
                            highlightedSkills = emptySet()
                        }
                    },
                    onAddCustom = { famille, index -> showAddCustomDialog = famille to index },
                    onManageCustom = { item -> showManageCustomDialog = item }
                )
            }
        }
    }

    // Dialogues de gestion des compétences custom
    showAddCustomDialog?.let { (famille, index) ->
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = null },
            title = { Text("Nouvelle compétence") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la compétence") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.ajouterCompetenceCustom(famille, index, name)
                            showAddCustomDialog = null
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.valider)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = null }) { Text(stringResource(R.string.annuler)) }
            }
        )
    }

    showManageCustomDialog?.let { item ->
        var newName by remember { mutableStateOf(item.competence.nom) }
        AlertDialog(
            onDismissRequest = { showManageCustomDialog = null },
            title = { Text("Gérer la compétence") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Renommer") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        item.customKey?.let { viewModel.supprimerCompetenceCustom(it) }
                        showManageCustomDialog = null
                    }) { Text("Supprimer", color = Color.Red) }
                    
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank() && newName != item.competence.nom) {
                                item.customKey?.let { viewModel.renommerCompetenceCustom(it, newName) }
                                showManageCustomDialog = null
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) { Text(stringResource(R.string.valider)) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showManageCustomDialog = null }) { Text(stringResource(R.string.annuler)) }
            }
        )
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

    messageDraconicBloque?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::effacerMessageDraconic,
            title = { Text(stringResource(R.string.competences_modif_impossible_titre)) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::effacerMessageDraconic) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompetencesContent(
    uiState: CompetencesUiState.Success,
    isXPBlocked: Boolean,
    highlightedSkills: Set<String>,
    onNaviguerVers: (EcranCreation) -> Unit,
    onAideRequise: (String) -> Unit,
    onCompetenceChange: (String, Int, Int) -> Unit,
    onTroncChange: (String, String, Int) -> Unit,
    onDraconicChange: (VoieDraconic, Int) -> Unit,
    onDragEnd: () -> Unit,
    onAtBorneChange: (CompetenceUiItem.Individuelle, Boolean) -> Unit,
    onAddCustom: (FamilleCompetence, Int) -> Unit,
    onManageCustom: (CompetenceUiItem.Individuelle) -> Unit
) {
    val pagerState = rememberPagerState { uiState.colonnes.size }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigationEcran(
                titre = stringResource(R.string.menu_competences),
                ecranCourant = EcranCreation.COMPETENCES,
                afficherSorts = uiState.aDesSortsAccessibles,
                onNaviguerVers = onNaviguerVers
            )

            // Indicateur de famille TabRow
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
                                fontFamily = Luminari,
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
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(colonne.items, key = { 
                        when(it) {
                            is CompetenceUiItem.Individuelle -> it.customKey ?: it.competence.nom
                            is CompetenceUiItem.Placeholder -> "placeholder_${it.famille.name}_${it.index}"
                        }
                    }) { item ->
                        when(item) {
                            is CompetenceUiItem.Individuelle -> {
                                CompetenceRow(
                                    nom = item.competence.nom,
                                    niveauActuel = item.niveauActuel,
                                    niveauBase = item.competence.niveauBase,
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
                                                onCompetenceChange(item.customKey ?: item.competence.nom, item.competence.niveauBase, nouveau)
                                        }
                                    },
                                    onAideRequise = { onAideRequise(item.competence.nom) },
                                    onDragEnd = onDragEnd,
                                    onAtBorneChange = { isAtBorne -> onAtBorneChange(item, isAtBorne) },
                                    isForceRed = item.competence.nom in highlightedSkills,
                                    isVerrouille = item.estVerrouilleParSorts,
                                    isCustom = item.isCustom,
                                    onCustomNameClick = { onManageCustom(item) },
                                    onVerrouilleClick = {
                                        val voie = VoieDraconic.entries.firstOrNull {
                                            it.name.lowercase().replaceFirstChar { c -> c.uppercase() } == item.competence.nom
                                        }
                                        if (voie != null) onDraconicChange(voie, item.niveauActuel)
                                    },
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                            is CompetenceUiItem.Placeholder -> {
                                PlaceholderRow(onClick = { onAddCustom(item.famille, item.index) })
                            }
                        }
                        HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.06f))
                    }
                }
            }
        }

        // Compteur de points restants — Sticky Bottom
        Surface(
            color = VoyageurColors.ParcheminBase.copy(alpha = 0.3f),
            shadowElevation = 0.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (uiState.hautRevant) {
                    Text(
                        text = stringResource(R.string.sorts_points_sorts, uiState.pointsSortsUtilises),
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoyageurColors.NomCaracteristique,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                val fullText = stringResource(R.string.xp_restants, uiState.pointsRestants)
                val valueStr = uiState.pointsRestants.toString()
                val startIndex = fullText.indexOf(valueStr)
                
                val annotatedText = buildAnnotatedString {
                    append(fullText)
                    if (startIndex != -1) {
                        addStyle(
                            style = SpanStyle(
                                color = if (isXPBlocked)
                                    Color(0xFFFF0000)
                                else if (uiState.pointsRestants < 0)
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
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun PlaceholderRow(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(0.10f))
        Text(
            text = ".........................",
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = VoyageurColors.NomCaracteristique.copy(alpha = 0.5f),
            modifier = Modifier.weight(0.50f)
        )
        Spacer(Modifier.weight(0.15f))
        Spacer(Modifier.weight(0.15f))
        Spacer(Modifier.weight(0.10f))
    }
}
