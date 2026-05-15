package com.jb.voyageur.feature.caracteristiques

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.usecase.ChampCaracteristique
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.helper.AideCaracteristiqueProvider
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.caracteristiques.composable.SectionDescription

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CaracteristiquesScreen(
    voyageurId: Long,
    onNaviguerVers: (EcranCreation) -> Unit,
    viewModel: CaracteristiquesViewModel = hiltViewModel()
) {
    BackHandler(enabled = true) {
        // Bloquer le retour système
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()
    val confirmationHautRevant by viewModel.confirmationHautRevant.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as android.app.Activity)

    confirmationHautRevant?.let { conf ->
        val displayedName = conf.nom ?: stringResource(conf.nomRes!!)
        AlertDialog(
            onDismissRequest = viewModel::annulerChangementHautRevant,
            title = { Text(stringResource(R.string.vrai_revant_confirmation_titre)) },
            text = { Text(stringResource(R.string.vrai_revant_confirmation_message, displayedName)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmerChangementHautRevant) {
                    Text(stringResource(R.string.oui))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::annulerChangementHautRevant) {
                    Text(stringResource(R.string.non))
                }
            }
        )
    }

    when (val state = uiState) {
        CaracteristiquesUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
            }
        }
        is CaracteristiquesUiState.Success -> {
            CaracteristiquesContent(
                uiState = state,
                aideActive = aideActive,
                windowWidthSizeClass = windowSizeClass.widthSizeClass,
                onNaviguerVers = onNaviguerVers,
                provider = viewModel.aideCaracteristiqueProvider,
                onCaracteristiqueChange = viewModel::onCaracteristiqueChange,
                onBeauteChange = viewModel::onBeauteChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onPoidsSaisi = viewModel::onPoidsSaisi,
                onTailleCmSaisie = viewModel::onTailleCmSaisie,
                onHeureNaissanceChange = viewModel::onHeureNaissanceChange,
                onLateraliteChange = viewModel::onLateraliteChange,
                onDemanderAide = viewModel::onDemanderAide,
                onFermerAide = viewModel::onFermerAide
            )
        }
    }
}

@Composable
fun CaracteristiquesContent(
    uiState: CaracteristiquesUiState.Success,
    aideActive: ChampAffichage?,
    windowWidthSizeClass: WindowWidthSizeClass,
    onNaviguerVers: (EcranCreation) -> Unit,
    modifier: Modifier = Modifier,
    provider: AideCaracteristiqueProvider,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onPoidsSaisi: (Int) -> Unit,
    onTailleCmSaisie: (Int) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit,
    onFermerAide: () -> Unit
) {
    ParcheminBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigationEcran(
                titre = stringResource(R.string.menu_caracteristiques),
                ecranCourant = EcranCreation.CARACTERISTIQUES,
                afficherSorts = uiState.aDesSortsAccessibles,
                onNaviguerVers = onNaviguerVers
            )

            if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
                Row(modifier = Modifier.fillMaxSize()) {
                    CaracteristiquesListe(
                        uiState = uiState,
                        modifier = Modifier.weight(0.6f),
                        onCaracteristiqueChange = onCaracteristiqueChange,
                        onBeauteChange = onBeauteChange,
                        onDescriptionChange = onDescriptionChange,
                        onPoidsSaisi = onPoidsSaisi,
                        onTailleCmSaisie = onTailleCmSaisie,
                        onHeureNaissanceChange = onHeureNaissanceChange,
                        onLateraliteChange = onLateraliteChange,
                        onDemanderAide = onDemanderAide
                    )
                    ZoneAide(
                        champAide = aideActive,
                        provider = provider,
                        modifier = Modifier.weight(0.4f)
                    )
                }
            } else {
                CaracteristiquesListe(
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize(),
                    onCaracteristiqueChange = onCaracteristiqueChange,
                    onBeauteChange = onBeauteChange,
                    onDescriptionChange = onDescriptionChange,
                    onPoidsSaisi = onPoidsSaisi,
                    onTailleCmSaisie = onTailleCmSaisie,
                    onHeureNaissanceChange = onHeureNaissanceChange,
                    onLateraliteChange = onLateraliteChange,
                    onDemanderAide = onDemanderAide
                )
                aideActive?.let { champ ->
                    AideBottomSheet(
                        champAide = champ,
                        provider = provider,
                        onDismiss = onFermerAide
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CaracteristiquesListe(
    uiState: CaracteristiquesUiState.Success,
    modifier: Modifier = Modifier,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onPoidsSaisi: (Int) -> Unit,
    onTailleCmSaisie: (Int) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit
) {
    var highlightedChamps by remember { mutableStateOf(setOf<ChampAffichage>()) }
    var highlightPointsRestants by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier) {
        item {
            SectionDescription(
                uiState = uiState,
                onDescriptionChange = onDescriptionChange,
                onPoidsSaisi = onPoidsSaisi,
                onTailleCmSaisie = onTailleCmSaisie,
                onBeauteChange = onBeauteChange,
                onHeureNaissanceChange = onHeureNaissanceChange,
                onDemanderAide = onDemanderAide,
                visible = true
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Colonne 1
                Column(
                    modifier = Modifier.weight(1.05f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val col1 = listOf(
                        ChampAffichage.Principale.TAILLE,
                        ChampAffichage.Principale.APPARENCE,
                        ChampAffichage.Principale.CONSTITUTION,
                        ChampAffichage.Principale.FORCE,
                        ChampAffichage.Principale.AGILITE,
                        ChampAffichage.Principale.DEXTERITE,
                        ChampAffichage.Principale.VUE,
                        ChampAffichage.Principale.OUIE,
                        ChampAffichage.Principale.ODO_GOUT
                    )
                    col1.forEach { champ ->
                        CaracteristiqueItem(
                            champ = champ,
                            uiState = uiState,
                            labelPaddingStart = 6.dp, 
                            valuePaddingEnd = 8.dp,
                            isForceRed = champ in highlightedChamps,
                            onAtBorneChange = { direction ->
                                if (direction == 1) { // Blocage en montant
                                    if (champ == ChampAffichage.Principale.FORCE && 
                                        uiState.caracteristiques.force == uiState.forceMax &&
                                        uiState.forceMax < 15) {
                                        highlightedChamps = setOf(ChampAffichage.Principale.TAILLE)
                                    } else if (uiState.pointsRestants == 0 && champ is ChampAffichage.Principale) {
                                        highlightPointsRestants = true
                                    }
                                } else {
                                    highlightedChamps = emptySet()
                                    highlightPointsRestants = false
                                }
                            },
                            onCaracteristiqueChange = onCaracteristiqueChange,
                            onDemanderAide = onDemanderAide
                        )
                    }
                }

                // Colonne 2
                Column(
                    modifier = Modifier.weight(0.95f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val col2 = listOf(
                        ChampAffichage.Principale.VOLONTE,
                        ChampAffichage.Principale.INTELLECT,
                        ChampAffichage.Principale.EMPATHIE,
                        ChampAffichage.Principale.REVE,
                        ChampAffichage.Principale.CHANCE,
                        ChampAffichage.Derivee.MELEE,
                        ChampAffichage.Derivee.TIR,
                        ChampAffichage.Derivee.LANCER,
                        ChampAffichage.Derivee.DEROBEE
                    )
                    col2.forEach { champ ->
                        CaracteristiqueItem(
                            champ = champ,
                            uiState = uiState,
                            valuePaddingEnd = 20.dp,
                            isForceRed = champ in highlightedChamps,
                            onAtBorneChange = { direction ->
                                if (direction == 1) { // Blocage en montant
                                    if (champ == ChampAffichage.Principale.FORCE && 
                                        uiState.caracteristiques.force == uiState.forceMax &&
                                        uiState.forceMax < 15) {
                                        highlightedChamps = setOf(ChampAffichage.Principale.TAILLE)
                                    } else if (uiState.pointsRestants == 0 && champ is ChampAffichage.Principale) {
                                        highlightPointsRestants = true
                                    }
                                } else {
                                    highlightedChamps = emptySet()
                                    highlightPointsRestants = false
                                }
                            },
                            onCaracteristiqueChange = onCaracteristiqueChange,
                            onDemanderAide = onDemanderAide
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp, end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val fullText = stringResource(R.string.points_restants, uiState.pointsRestants)
                val valueStr = uiState.pointsRestants.toString()
                val startIndex = fullText.indexOf(valueStr)
                
                val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
                    append(fullText)
                    if (startIndex != -1) {
                        addStyle(
                            style = androidx.compose.ui.text.SpanStyle(
                                color = if (highlightPointsRestants) androidx.compose.ui.graphics.Color(0xFFFF0000)
                                        else if (uiState.pointsRestants < 0) VoyageurColors.ValeurCaracteristique 
                                        else androidx.compose.ui.graphics.Color.Black,
                                fontWeight = FontWeight.Bold
                            ),
                            start = startIndex,
                            end = startIndex + valueStr.length
                        )
                    }
                }

                Text(
                    text = annotatedString,
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = VoyageurColors.NomCaracteristique,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.VIE, uiState.vie, onDemanderAide) }
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.ENDURANCE, uiState.endurance, onDemanderAide) }
                }
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.SC, uiState.sc, onDemanderAide) }
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.SUST, uiState.sust, onDemanderAide) }
                }
                Row(Modifier.fillMaxWidth()) {
                    val domText = if (uiState.bonusDom > 0) "+${uiState.bonusDom}" else uiState.bonusDom.toString()
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.BONUS_DOM, domText, onDemanderAide) }
                    Box(Modifier.weight(1f)) { ThresholdItem(ChampAffichage.Seuil.ENCOMBREMENT, String.format("%.1f", uiState.encombrement), onDemanderAide) }
                }
            }
        }
    }
}

@Composable
private fun CaracteristiqueItem(
    champ: ChampAffichage,
    uiState: CaracteristiquesUiState.Success,
    labelPaddingStart: androidx.compose.ui.unit.Dp = 0.dp,
    valuePaddingEnd: androidx.compose.ui.unit.Dp = 0.dp,
    isForceRed: Boolean = false,
    onAtBorneChange: (Int) -> Unit = {},
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit
) {
    val valeur = when (champ) {
        is ChampAffichage.Principale -> when (champ) {
            ChampAffichage.Principale.TAILLE -> uiState.caracteristiques.taille
            ChampAffichage.Principale.APPARENCE -> uiState.caracteristiques.apparence
            ChampAffichage.Principale.CONSTITUTION -> uiState.caracteristiques.constitution
            ChampAffichage.Principale.FORCE -> uiState.caracteristiques.force
            ChampAffichage.Principale.AGILITE -> uiState.caracteristiques.agilite
            ChampAffichage.Principale.DEXTERITE -> uiState.caracteristiques.dexterite
            ChampAffichage.Principale.VUE -> uiState.caracteristiques.vue
            ChampAffichage.Principale.OUIE -> uiState.caracteristiques.ouie
            ChampAffichage.Principale.ODO_GOUT -> uiState.caracteristiques.odoGout
            ChampAffichage.Principale.VOLONTE -> uiState.caracteristiques.volonte
            ChampAffichage.Principale.INTELLECT -> uiState.caracteristiques.intellect
            ChampAffichage.Principale.EMPATHIE -> uiState.caracteristiques.empathie
            ChampAffichage.Principale.REVE -> uiState.caracteristiques.reve
            ChampAffichage.Principale.CHANCE -> uiState.caracteristiques.chance
        }
        is ChampAffichage.Derivee -> when (champ) {
            ChampAffichage.Derivee.MELEE -> uiState.melee
            ChampAffichage.Derivee.TIR -> uiState.tir
            ChampAffichage.Derivee.LANCER -> uiState.lancer
            ChampAffichage.Derivee.DEROBEE -> uiState.derobee
        }
        else -> 0
    }

    val labelRes = when (champ) {
        is ChampAffichage.Principale -> when(champ) {
            ChampAffichage.Principale.TAILLE -> R.string.carac_taille
            ChampAffichage.Principale.APPARENCE -> R.string.carac_apparence
            ChampAffichage.Principale.CONSTITUTION -> R.string.carac_constitution
            ChampAffichage.Principale.FORCE -> R.string.carac_force
            ChampAffichage.Principale.AGILITE -> R.string.carac_agilite
            ChampAffichage.Principale.DEXTERITE -> R.string.carac_dexterite
            ChampAffichage.Principale.VUE -> R.string.carac_vue
            ChampAffichage.Principale.OUIE -> R.string.carac_ouie
            ChampAffichage.Principale.ODO_GOUT -> R.string.carac_odogout
            ChampAffichage.Principale.VOLONTE -> R.string.carac_volonte
            ChampAffichage.Principale.INTELLECT -> R.string.carac_intellect
            ChampAffichage.Principale.EMPATHIE -> R.string.carac_empathie
            ChampAffichage.Principale.REVE -> R.string.carac_reve
            ChampAffichage.Principale.CHANCE -> R.string.carac_chance
        }
        is ChampAffichage.Derivee -> when(champ) {
            ChampAffichage.Derivee.MELEE -> R.string.derivee_melee
            ChampAffichage.Derivee.TIR -> R.string.derivee_tir
            ChampAffichage.Derivee.LANCER -> R.string.derivee_lancer
            ChampAffichage.Derivee.DEROBEE -> R.string.derivee_derobee
        }
        else -> R.string.app_name
    }

    val max = if (champ is ChampAffichage.Principale) {
        val baseMax = if (champ == ChampAffichage.Principale.FORCE) uiState.forceMax else 15
        minOf(baseMax, valeur + uiState.pointsRestants)
    } else {
        valeur
    }
    val min = if (champ is ChampAffichage.Derivee) valeur else 6

    CaracteristiqueRow(
        nom = stringResource(labelRes),
        valeur = valeur,
        min = min,
        max = max,
        labelPaddingStart = labelPaddingStart,
        valuePaddingEnd = valuePaddingEnd,
        isForceRed = isForceRed,
        onValeurChange = { if (champ is ChampAffichage.Principale) onCaracteristiqueChange(champ.domain, it) },
        onAtBorneChange = onAtBorneChange,
        onAideRequise = { onDemanderAide(champ) }
    )
}

@Composable
private fun ThresholdItem(
    champ: ChampAffichage.Seuil,
    valeur: Any,
    onDemanderAide: (ChampAffichage) -> Unit
) {
    val labelRes = when(champ) {
        ChampAffichage.Seuil.VIE -> R.string.seuil_vie
        ChampAffichage.Seuil.ENDURANCE -> R.string.seuil_endurance
        ChampAffichage.Seuil.SC -> R.string.seuil_sc
        ChampAffichage.Seuil.SUST -> R.string.seuil_sust
        ChampAffichage.Seuil.BONUS_DOM -> R.string.seuil_bonus_dom
        ChampAffichage.Seuil.ENCOMBREMENT -> R.string.seuil_encombrement
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onDemanderAide(champ) }
            .padding(vertical = 0.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(labelRes), fontFamily = FontFamily.Serif, fontSize = 16.sp, color = VoyageurColors.NomCaracteristique)
        Text(text = valeur.toString(), fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Normal, color = VoyageurColors.ValeurCaracteristique)
    }
}

@Composable
fun ZoneAide(
    champAide: ChampAffichage?,
    provider: AideCaracteristiqueProvider,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        if (champAide != null) {
            val context = LocalContext.current
            val aide = remember(champAide) {
                provider.pour(champAide, context.resources)
            }
            Text(text = aide.titre, fontFamily = FontFamily.Serif, fontSize = 24.sp)
            if (aide.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = aide.description, fontFamily = FontFamily.SansSerif, fontSize = 16.sp)
            }
        } else {
            Text(text = stringResource(R.string.aide_invitation))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AideBottomSheet(
    champAide: ChampAffichage,
    provider: AideCaracteristiqueProvider,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        ZoneAide(champAide = champAide, provider = provider)
        Spacer(Modifier.height(24.dp))
    }
}
