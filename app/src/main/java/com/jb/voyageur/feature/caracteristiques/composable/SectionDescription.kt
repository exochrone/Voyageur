package com.jb.voyageur.feature.caracteristiques.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.model.Lateralite
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.HeureNaissancePicker
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.caracteristiques.ChampAide
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesUiState

@Composable
fun SectionDescription(
    uiState: CaracteristiquesUiState.Success,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onLateraliteChange: (Lateralite) -> Unit,
    onHeureNaissanceChange: (HeureNaissance) -> Unit,
    onDemanderAide: (ChampAide) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var editChamp by remember { mutableStateOf<Pair<ChampDescription, String>?>(null) }
    val context = LocalContext.current

    AnimatedVisibility(visible = visible) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Zone gauche : champs description
                Column(Modifier.weight(1f)) {
                    // Sexe
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.homme), 
                            modifier = Modifier.clickable { onDescriptionChange(ChampDescription.SEXE, Sexe.HOMME.name) },
                            fontWeight = if(uiState.sexe == Sexe.HOMME) FontWeight.Bold else FontWeight.Normal,
                            color = if(uiState.sexe == Sexe.HOMME) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique
                        )
                        Text(text = "/")
                        Text(text = stringResource(R.string.femme), 
                            modifier = Modifier.clickable { onDescriptionChange(ChampDescription.SEXE, Sexe.FEMME.name) },
                            fontWeight = if(uiState.sexe == Sexe.FEMME) FontWeight.Bold else FontWeight.Normal,
                            color = if(uiState.sexe == Sexe.FEMME) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique
                        )
                    }

                    DescriptionLabel(label = stringResource(R.string.description_age), valeur = uiState.age?.toString() ?: "") {
                        editChamp = ChampDescription.AGE to (uiState.age?.toString() ?: "")
                    }
                    DescriptionLabel(label = stringResource(R.string.description_taille_cm), valeur = uiState.tailleCm?.toString() ?: "") {
                        editChamp = ChampDescription.TAILLE_CM to (uiState.tailleCm?.toString() ?: "")
                    }
                    DescriptionLabel(label = stringResource(R.string.description_poids_kg), valeur = uiState.poidsKg?.toString() ?: "") {
                        editChamp = ChampDescription.POIDS_KG to (uiState.poidsKg?.toString() ?: "")
                    }
                    DescriptionLabel(label = stringResource(R.string.description_cheveux), valeur = uiState.cheveux) {
                        editChamp = ChampDescription.CHEVEUX to uiState.cheveux
                    }
                    DescriptionLabel(label = stringResource(R.string.description_yeux), valeur = uiState.yeux) {
                        editChamp = ChampDescription.YEUX to uiState.yeux
                    }
                    DescriptionLabel(label = stringResource(R.string.description_signe_particulier), valeur = uiState.signeParticulier) {
                        editChamp = ChampDescription.SIGNE_PARTICULIER to uiState.signeParticulier
                    }

                    // Latéralité
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(R.string.lateralite) + " :", color = VoyageurColors.NomCaracteristique)
                        Text(text = stringResource(R.string.droitier), 
                            modifier = Modifier.clickable { onLateraliteChange(Lateralite.DROITIER) },
                            fontWeight = if(uiState.lateralite == Lateralite.DROITIER) FontWeight.Bold else FontWeight.Normal,
                            color = if(uiState.lateralite == Lateralite.DROITIER) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique
                        )
                        Text(text = stringResource(R.string.gaucher), 
                            modifier = Modifier.clickable { onLateraliteChange(Lateralite.GAUCHER) },
                            fontWeight = if(uiState.lateralite == Lateralite.GAUCHER) FontWeight.Bold else FontWeight.Normal,
                            color = if(uiState.lateralite == Lateralite.GAUCHER) VoyageurColors.ValeurCaracteristique else VoyageurColors.NomCaracteristique
                        )
                    }

                    // Beauté avec Adjectif
                    val beauteAdjectif = stringResource(id = context.resources.getIdentifier("beaute_${uiState.beaute}", "string", context.packageName))
                    CaracteristiqueRow(
                        nom = stringResource(R.string.aide_beaute_titre),
                        valeur = uiState.beaute,
                        min = 3,
                        max = 16,
                        valeurDisplay = "${uiState.beaute} ($beauteAdjectif)",
                        onValeurChange = onBeauteChange,
                        onAideRequise = { onDemanderAide(ChampAide.Beaute) },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Zone droite : heure de naissance
                HeureNaissancePicker(
                    heureCourante = uiState.heureNaissance,
                    onHeureChange = onHeureNaissanceChange,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(start = 16.dp, top = 8.dp)
                )
            }
            HorizontalDivider()
        }
    }

    editChamp?.let { (champ, valeur) ->
        var tempValeur by remember { mutableStateOf(valeur) }
        val isNumeric = champ in listOf(ChampDescription.AGE, ChampDescription.TAILLE_CM, ChampDescription.POIDS_KG)
        
        AlertDialog(
            onDismissRequest = { editChamp = null },
            title = { Text(stringResource(R.string.dialog_edit_title, stringResource(getLabelRes(champ)))) },
            text = {
                OutlinedTextField(
                    value = tempValeur,
                    onValueChange = { tempValeur = it },
                    keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDescriptionChange(champ, tempValeur)
                    editChamp = null
                }) { Text(stringResource(R.string.valider)) }
            },
            dismissButton = {
                TextButton(onClick = { editChamp = null }) { Text(stringResource(R.string.annuler)) }
            }
        )
    }
}

@Composable
fun DescriptionLabel(label: String, valeur: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(text = "$label : ", color = VoyageurColors.NomCaracteristique)
        Text(text = valeur, color = VoyageurColors.ValeurCaracteristique, fontWeight = FontWeight.Bold)
    }
}

private fun getLabelRes(champ: ChampDescription): Int = when (champ) {
    ChampDescription.NOM -> R.string.description_nom
    ChampDescription.CHEVEUX -> R.string.description_cheveux
    ChampDescription.YEUX -> R.string.description_yeux
    ChampDescription.SIGNE_PARTICULIER -> R.string.description_signe_particulier
    ChampDescription.AGE -> R.string.description_age
    ChampDescription.TAILLE_CM -> R.string.description_taille_cm
    ChampDescription.POIDS_KG -> R.string.description_poids_kg
    ChampDescription.SEXE -> R.string.homme // Not used for dialog but for completeness
}
