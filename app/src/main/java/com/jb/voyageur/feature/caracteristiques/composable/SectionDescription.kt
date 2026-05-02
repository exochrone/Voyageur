package com.jb.voyageur.feature.caracteristiques.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.HeureNaissancePicker
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.caracteristiques.ChampAffichage
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesUiState

@Composable
fun SectionDescription(
    uiState: CaracteristiquesUiState.Success,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onHeureNaissanceChange: (HeureNaissance) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var editChamp by remember { mutableStateOf<Pair<ChampDescription, String>?>(null) }

    AnimatedVisibility(visible = visible) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // ── Ligne 1 — Sélecteurs pleine largeur ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sexe
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.homme),
                        fontWeight = if (uiState.sexe == Sexe.HOMME) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.sexe == Sexe.HOMME) VoyageurColors.NomCaracteristique else Color.Gray,
                        modifier = Modifier.clickable {
                            onDescriptionChange(ChampDescription.SEXE, Sexe.HOMME.name)
                        }
                    )
                    Text(text = "/", color = Color.Gray)
                    Text(
                        text = stringResource(R.string.femme),
                        fontWeight = if (uiState.sexe == Sexe.FEMME) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.sexe == Sexe.FEMME) VoyageurColors.NomCaracteristique else Color.Gray,
                        modifier = Modifier.clickable {
                            onDescriptionChange(ChampDescription.SEXE, Sexe.FEMME.name)
                        }
                    )
                }

                // Haut-rêvant
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.vrai_revant),
                        fontWeight = if (!uiState.hautRevant) FontWeight.Bold else FontWeight.Normal,
                        color = if (!uiState.hautRevant) VoyageurColors.NomCaracteristique else Color.Gray,
                        modifier = Modifier.clickable {
                            onDescriptionChange(ChampDescription.HAUT_REVANT, "false")
                        }
                    )
                    Text(text = "/", color = Color.Gray)
                    Text(
                        text = stringResource(R.string.haut_revant),
                        fontWeight = if (uiState.hautRevant) FontWeight.Bold else FontWeight.Normal,
                        color = if (uiState.hautRevant) VoyageurColors.NomCaracteristique else Color.Gray,
                        modifier = Modifier.clickable {
                            onDescriptionChange(ChampDescription.HAUT_REVANT, "true")
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Lignes 2 et 3 — Corps + Heure de naissance ──────────
            Row(modifier = Modifier.fillMaxWidth()) {

                // Zone gauche 75%
                Column(modifier = Modifier.weight(0.75f)) {

                    // Ligne 2 : Âge / Taille / Poids
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_age),
                                valeur = uiState.age?.toString() ?: ""
                            ) { editChamp = ChampDescription.AGE to (uiState.age?.toString() ?: "") }
                        }
                        Box(Modifier.weight(1f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_taille),
                                valeur = uiState.tailleCm?.toString() ?: ""
                            ) { editChamp = ChampDescription.TAILLE_CM to (uiState.tailleCm?.toString() ?: "") }
                        }
                        Box(Modifier.weight(1f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_poids),
                                valeur = uiState.poidsKg?.toString() ?: ""
                            ) { editChamp = ChampDescription.POIDS_KG to (uiState.poidsKg?.toString() ?: "") }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Ligne 3 : Cheveux / Yeux
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_cheveux),
                                valeur = uiState.cheveux
                            ) { editChamp = ChampDescription.CHEVEUX to uiState.cheveux }
                        }
                        Box(Modifier.weight(1f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_yeux),
                                valeur = uiState.yeux
                            ) { editChamp = ChampDescription.YEUX to uiState.yeux }
                        }
                    }
                }

                // Zone droite 25% — Heure de naissance
                // Occupe la hauteur des lignes 2 et 3 ensemble
                Box(
                    modifier = Modifier
                        .weight(0.25f)
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    HeureNaissancePicker(
                        heureCourante = uiState.heureNaissance,
                        onHeureChange = onHeureNaissanceChange
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Ligne 4 — Signe particulier ──────────────────────────
            DescriptionLabel(
                label = stringResource(R.string.description_signes_particuliers),
                valeur = uiState.signeParticulier,
                modifier = Modifier.fillMaxWidth()
            ) { editChamp = ChampDescription.SIGNE_PARTICULIER to uiState.signeParticulier }

            Spacer(Modifier.height(4.dp))

            // ── Ligne 5 — Beauté ──────────────────────────────────────
            val beauteAdjectif = when (uiState.beaute) {
                3 -> stringResource(R.string.beaute_3)
                4 -> stringResource(R.string.beaute_4)
                5 -> stringResource(R.string.beaute_5)
                6 -> stringResource(R.string.beaute_6)
                7 -> stringResource(R.string.beaute_7)
                8 -> stringResource(R.string.beaute_8)
                9 -> stringResource(R.string.beaute_9)
                10 -> stringResource(R.string.beaute_10)
                11 -> stringResource(R.string.beaute_11)
                12 -> stringResource(R.string.beaute_12)
                13 -> stringResource(R.string.beaute_13)
                14 -> stringResource(R.string.beaute_14)
                15 -> stringResource(R.string.beaute_15)
                16 -> stringResource(R.string.beaute_16)
                else -> ""
            }

            CaracteristiqueRow(
                nom = stringResource(R.string.aide_beaute_titre),
                valeur = uiState.beaute,
                min = 3,
                max = 16,
                valeurDisplay = "${uiState.beaute} — $beauteAdjectif",
                labelFontFamily = FontFamily.Serif,
                valueFontFamily = FontFamily.Serif,
                onValeurChange = onBeauteChange,
                onAideRequise = { onDemanderAide(ChampAffichage.Beaute) }
            )
            HorizontalDivider()
        }
    }

    editChamp?.let { (champ, valeur) ->
        var tempValeur by remember { mutableStateOf(valeur) }
        val isNumeric = champ in listOf(ChampDescription.AGE, ChampDescription.TAILLE_CM, ChampDescription.POIDS_KG)
        
        val dialogTitle = when(champ) {
            ChampDescription.TAILLE_CM -> stringResource(R.string.description_taille) + " (cm)"
            ChampDescription.POIDS_KG -> stringResource(R.string.description_poids) + " (kg)"
            else -> stringResource(getLabelRes(champ))
        }

        AlertDialog(
            onDismissRequest = { editChamp = null },
            title = { Text(stringResource(R.string.dialog_edit_title, dialogTitle)) },
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
fun DescriptionLabel(label: String, valeur: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(text = "$label ", color = VoyageurColors.NomCaracteristique)
        Text(text = valeur, color = VoyageurColors.ValeurCaracteristique, fontWeight = FontWeight.Bold)
    }
}

private fun getLabelRes(champ: ChampDescription): Int = when (champ) {
    ChampDescription.NOM -> R.string.description_nom
    ChampDescription.CHEVEUX -> R.string.description_cheveux
    ChampDescription.YEUX -> R.string.description_yeux
    ChampDescription.SIGNE_PARTICULIER -> R.string.description_signes_particuliers
    ChampDescription.AGE -> R.string.description_age
    ChampDescription.TAILLE_CM -> R.string.description_taille
    ChampDescription.POIDS_KG -> R.string.description_poids
    ChampDescription.SEXE -> R.string.homme
    ChampDescription.HAUT_REVANT -> R.string.haut_revant
}
