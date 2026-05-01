package com.jb.voyageur.feature.caracteristiques

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.jb.voyageur.core.ui.helper.AideCaracteristiqueProvider
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.caracteristiques.composable.SectionDescription

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CaracteristiquesScreen(
    voyageurId: Long,
    viewModel: CaracteristiquesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as android.app.Activity)

    var descriptionDepliee by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaracteristiquesEvent.ConfirmerPerteBeaute -> {
                    // Confirmation beauté enlevée
                }
            }
        }
    }

    Scaffold(
        topBar = {
            val state = uiState
            if (state is CaracteristiquesUiState.Success) {
                TopAppBar(
                    title = {
                        Text(
                            text = state.nom.ifBlank { stringResource(R.string.section_description) },
                            fontFamily = GoudyAcc
                        )
                    },
                    actions = {
                        IconButton(onClick = { descriptionDepliee = !descriptionDepliee }) {
                            Icon(
                                imageVector = if (descriptionDepliee)
                                    Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            CaracteristiquesUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Chargement...")
                }
            }
            is CaracteristiquesUiState.Success -> {
                CaracteristiquesContent(
                    uiState = state,
                    aideActive = aideActive,
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    descriptionDepliee = descriptionDepliee,
                    modifier = Modifier.padding(innerPadding),
                    onCaracteristiqueChange = viewModel::onCaracteristiqueChange,
                    onBeauteChange = viewModel::onBeauteChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onHeureNaissanceChange = viewModel::onHeureNaissanceChange,
                    onLateraliteChange = viewModel::onLateraliteChange,
                    onDemanderAide = viewModel::onDemanderAide,
                    onFermerAide = viewModel::onFermerAide
                )
            }
        }
    }
}

@Composable
fun CaracteristiquesContent(
    uiState: CaracteristiquesUiState.Success,
    aideActive: ChampAffichage?,
    windowWidthSizeClass: WindowWidthSizeClass,
    descriptionDepliee: Boolean,
    modifier: Modifier = Modifier,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (com.jb.voyageur.core.domain.usecase.ChampDescription, String) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit,
    onFermerAide: () -> Unit
) {
    ParcheminBackground(modifier = modifier) {
        if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                CaracteristiquesListe(
                    uiState = uiState,
                    descriptionDepliee = descriptionDepliee,
                    modifier = Modifier.weight(0.6f),
                    onCaracteristiqueChange = onCaracteristiqueChange,
                    onBeauteChange = onBeauteChange,
                    onDescriptionChange = onDescriptionChange,
                    onHeureNaissanceChange = onHeureNaissanceChange,
                    onLateraliteChange = onLateraliteChange,
                    onDemanderAide = onDemanderAide
                )
                ZoneAide(
                    champAide = aideActive,
                    modifier = Modifier.weight(0.4f)
                )
            }
        } else {
            CaracteristiquesListe(
                uiState = uiState,
                descriptionDepliee = descriptionDepliee,
                modifier = Modifier.fillMaxSize(),
                onCaracteristiqueChange = onCaracteristiqueChange,
                onBeauteChange = onBeauteChange,
                onDescriptionChange = onDescriptionChange,
                onHeureNaissanceChange = onHeureNaissanceChange,
                onLateraliteChange = onLateraliteChange,
                onDemanderAide = onDemanderAide
            )
            aideActive?.let { champ ->
                AideBottomSheet(
                    champAide = champ,
                    onDismiss = onFermerAide
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CaracteristiquesListe(
    uiState: CaracteristiquesUiState.Success,
    descriptionDepliee: Boolean,
    modifier: Modifier = Modifier,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (com.jb.voyageur.core.domain.usecase.ChampDescription, String) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit
) {
    LazyColumn(modifier = modifier) {
        item {
            SectionDescription(
                uiState = uiState,
                onDescriptionChange = onDescriptionChange,
                onBeauteChange = onBeauteChange,
                onHeureNaissanceChange = onHeureNaissanceChange,
                onDemanderAide = onDemanderAide,
                visible = descriptionDepliee
            )
        }

        stickyHeader {
            Surface(
                color = VoyageurColors.ParcheminBase.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.points_restants, uiState.pointsRestants),
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    color = if (uiState.pointsRestants < 0) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Colonne 1
                Column(Modifier.weight(1.1f)) {
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
                            onCaracteristiqueChange = onCaracteristiqueChange,
                            onDemanderAide = onDemanderAide
                        )
                    }
                }

                // Colonne 2
                Column(Modifier.weight(0.9f)) {
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
                            valuePaddingEnd = 12.dp,
                            onCaracteristiqueChange = onCaracteristiqueChange,
                            onDemanderAide = onDemanderAide
                        )
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                
                ThresholdItem(ChampAffichage.Seuil.VIE, uiState.vie, onDemanderAide)
                ThresholdItem(ChampAffichage.Seuil.ENDURANCE, uiState.endurance, onDemanderAide)
                ThresholdItem(ChampAffichage.Seuil.SC, uiState.sc, onDemanderAide)
                ThresholdItem(ChampAffichage.Seuil.SUST, uiState.sust, onDemanderAide)
                ThresholdItem(ChampAffichage.Seuil.BONUS_DOM, uiState.bonusDom, onDemanderAide)
                ThresholdItem(ChampAffichage.Seuil.ENCOMBREMENT, String.format("%.1f", uiState.encombrement), onDemanderAide)
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

    val max = if (champ == ChampAffichage.Principale.FORCE) uiState.forceMax 
              else if (champ is ChampAffichage.Derivee) valeur 
              else 15
    val min = if (champ is ChampAffichage.Derivee) valeur else 6

    CaracteristiqueRow(
        nom = stringResource(labelRes),
        valeur = valeur,
        min = min,
        max = max,
        labelPaddingStart = labelPaddingStart,
        valuePaddingEnd = valuePaddingEnd,
        onValeurChange = { if (champ is ChampAffichage.Principale) onCaracteristiqueChange(champ.domain, it) },
        onAideRequise = { onDemanderAide(champ) }
    )
    HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(labelRes), fontFamily = FontFamily.Serif, color = VoyageurColors.NomCaracteristique)
        Text(text = valeur.toString(), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = VoyageurColors.ValeurCaracteristique)
    }
}

@Composable
fun ZoneAide(
    champAide: ChampAffichage?,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        if (champAide != null) {
            val context = LocalContext.current
            val aide = remember(champAide) {
                AideCaracteristiqueProvider.pour(champAide, context.resources)
            }
            Text(text = aide.titre, fontFamily = GoudyAcc, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = aide.description, fontFamily = FontFamily.Serif, fontSize = 16.sp)
        } else {
            Text(text = stringResource(R.string.aide_invitation))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AideBottomSheet(
    champAide: ChampAffichage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        ZoneAide(champAide = champAide)
        Spacer(Modifier.height(24.dp))
    }
}
