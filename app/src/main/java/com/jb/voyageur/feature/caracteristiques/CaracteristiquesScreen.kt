package com.jb.voyageur.feature.caracteristiques

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jb.voyageur.core.ui.AideCaracteristiqueProvider
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.caracteristiques.composable.SectionDescription

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CaracteristiquesScreen(
    voyageurId: Long,
    viewModel: CaracteristiquesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aideActive by viewModel.aideActive.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as android.app.Activity)

    var showConfirmBeaute by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaracteristiquesEvent.ConfirmerPerteBeaute ->
                    showConfirmBeaute = event.pointsPerdus
            }
        }
    }

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

    showConfirmBeaute?.let { pointsPerdus ->
        AlertDialog(
            onDismissRequest = { viewModel.onAnnulerPerteBeaute(); showConfirmBeaute = null },
            title = { Text(stringResource(R.string.beaute_confirmation_titre)) },
            text = {
                Text(stringResource(R.string.beaute_confirmation_texte, pointsPerdus))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onConfirmerPerteBeaute()
                    showConfirmBeaute = null
                }) { Text(stringResource(R.string.confirmer)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onAnnulerPerteBeaute()
                    showConfirmBeaute = null
                }) { Text(stringResource(R.string.annuler)) }
            }
        )
    }
}

@Composable
fun CaracteristiquesContent(
    uiState: CaracteristiquesUiState.Success,
    aideActive: ChampAide?,
    windowWidthSizeClass: WindowWidthSizeClass,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (com.jb.voyageur.core.domain.usecase.ChampDescription, String) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAide) -> Unit,
    onFermerAide: () -> Unit
) {
    ParcheminBackground {
        if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
            Row(Modifier.fillMaxSize()) {
                CaracteristiquesListe(
                    uiState = uiState,
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

@Composable
fun CaracteristiquesListe(
    uiState: CaracteristiquesUiState.Success,
    modifier: Modifier = Modifier,
    onCaracteristiqueChange: (ChampCaracteristique, Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onDescriptionChange: (com.jb.voyageur.core.domain.usecase.ChampDescription, String) -> Unit,
    onHeureNaissanceChange: (com.jb.voyageur.core.domain.model.HeureNaissance) -> Unit,
    onLateraliteChange: (com.jb.voyageur.core.domain.model.Lateralite) -> Unit,
    onDemanderAide: (ChampAide) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        SectionDescription(
            uiState = uiState,
            onDescriptionChange = onDescriptionChange,
            onHeureNaissanceChange = onHeureNaissanceChange,
            onLateraliteChange = onLateraliteChange
        )

        Surface(
            color = VoyageurColors.ParcheminBase.copy(alpha = 0.9f),
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

        Column(Modifier.padding(16.dp)) {
            ChampCaracteristique.entries.forEach { champ ->
                val valeur = when (champ) {
                    ChampCaracteristique.TAILLE -> uiState.caracteristiques.taille
                    ChampCaracteristique.APPARENCE -> uiState.caracteristiques.apparence
                    ChampCaracteristique.CONSTITUTION -> uiState.caracteristiques.constitution
                    ChampCaracteristique.FORCE -> uiState.caracteristiques.force
                    ChampCaracteristique.AGILITE -> uiState.caracteristiques.agilite
                    ChampCaracteristique.DEXTERITE -> uiState.caracteristiques.dexterite
                    ChampCaracteristique.VUE -> uiState.caracteristiques.vue
                    ChampCaracteristique.OUIE -> uiState.caracteristiques.ouie
                    ChampCaracteristique.ODO_GOUT -> uiState.caracteristiques.odoGout
                    ChampCaracteristique.VOLONTE -> uiState.caracteristiques.volonte
                    ChampCaracteristique.INTELLECT -> uiState.caracteristiques.intellect
                    ChampCaracteristique.EMPATHIE -> uiState.caracteristiques.empathie
                    ChampCaracteristique.REVE -> uiState.caracteristiques.reve
                    ChampCaracteristique.CHANCE -> uiState.caracteristiques.chance
                }
                
                val max = if (champ == ChampCaracteristique.FORCE) uiState.forceMax else 15

                CaracteristiqueRow(
                    nom = champ.name,
                    valeur = valeur,
                    max = max,
                    onValeurChange = { onCaracteristiqueChange(champ, it) },
                    onAideRequise = { onDemanderAide(ChampAide.Carac(champ)) }
                )
                HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.1f))
            }

            Spacer(Modifier.height(8.dp))
            CaracteristiqueRow(
                nom = stringResource(R.string.aide_beaute_titre),
                valeur = uiState.beaute,
                min = 1,
                max = 16,
                onValeurChange = onBeauteChange,
                onAideRequise = { onDemanderAide(ChampAide.Beaute) }
            )
            
            Spacer(Modifier.height(16.dp))
            Text(text = "Valeurs Dérivées", fontFamily = GoudyAcc, fontSize = 20.sp)
            DeriveeRow("Mêlée", uiState.melee)
            DeriveeRow("Tir", uiState.tir)
            DeriveeRow("Lancer", uiState.lancer)
            DeriveeRow("Dérobée", uiState.derobee)
            
            Spacer(Modifier.height(16.dp))
            Text(text = "Seuils et Points", fontFamily = GoudyAcc, fontSize = 20.sp)
            DeriveeRow("Vie", uiState.vie)
            DeriveeRow("Endurance", uiState.endurance)
            DeriveeRow("Seuil Constitution (SC)", uiState.sc)
            DeriveeRow("Sustentation", uiState.sust)
            DeriveeRow("Bonus Dommages", uiState.bonusDom)
            DeriveeRow("Encombrement", uiState.encombrement.toString())
        }
    }
}

@Composable
fun DeriveeRow(label: String, valeur: Any) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = FontFamily.Serif, color = VoyageurColors.NomCaracteristique)
        Text(text = valeur.toString(), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = VoyageurColors.ValeurCaracteristique)
    }
}

@Composable
fun ZoneAide(
    champAide: ChampAide?,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        if (champAide != null) {
            val context = LocalContext.current
            val aide = remember(champAide) {
                when (champAide) {
                    is ChampAide.Carac -> AideCaracteristiqueProvider.pour(champAide.champ, context.resources)
                    ChampAide.Beaute -> com.jb.voyageur.core.ui.AideCaracteristique(
                        ChampCaracteristique.APPARENCE, // peu importe ici
                        context.getString(R.string.aide_beaute_titre),
                        context.getString(R.string.aide_beaute_desc)
                    )
                }
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
    champAide: ChampAide,
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
