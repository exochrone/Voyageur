package com.jb.voyageur.feature.caracteristiques.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.ui.composable.CaracteristiqueRow
import com.jb.voyageur.core.ui.composable.HeureNaissancePicker
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.core.ui.util.FormatPhysique
import com.jb.voyageur.feature.caracteristiques.ChampAffichage
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesUiState

@Composable
fun SectionDescription(
    uiState: CaracteristiquesUiState.Success,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onPoidsSaisi: (Int) -> Unit,
    onTailleCmSaisie: (Int) -> Unit,
    onBeauteChange: (Int) -> Unit,
    onHeureNaissanceChange: (HeureNaissance) -> Unit,
    onDemanderAide: (ChampAffichage) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var editChamp by remember { mutableStateOf<Pair<ChampDescription, String>?>(null) }
    val spacing = 2.dp // Consistent vertical interval

    AnimatedVisibility(visible = visible) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 2.dp)
        ) {
            // ── Zone principale : 80% gauche + 20% heure ─────────────
            Row(modifier = Modifier.fillMaxWidth()) {

                // Colonne principale — 80%
                Column(modifier = Modifier.weight(0.80f)) {

                    // Ligne 1 — Vrai-rêvant / Haut-rêvant
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing)
                    ) {
                        Text(
                            text = stringResource(R.string.vrai_revant),
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (!uiState.hautRevant) FontWeight.Bold else FontWeight.Normal,
                            color = if (!uiState.hautRevant) VoyageurColors.NomCaracteristique else Color.Gray,
                            modifier = Modifier.clickable {
                                onDescriptionChange(ChampDescription.HAUT_REVANT, "false")
                            }
                        )
                        Text(text = "/", color = Color.Gray, fontFamily = FontFamily.Serif, fontSize = 14.sp)
                        Text(
                            text = stringResource(R.string.haut_revant),
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (uiState.hautRevant) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.hautRevant) VoyageurColors.NomCaracteristique else Color.Gray,
                            modifier = Modifier.clickable {
                                onDescriptionChange(ChampDescription.HAUT_REVANT, "true")
                            }
                        )
                    }

                    // Ligne 2 — Homme/Femme + Âge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(0.70f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.homme),
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (uiState.sexe == Sexe.HOMME) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.sexe == Sexe.HOMME) VoyageurColors.NomCaracteristique else Color.Gray,
                                modifier = Modifier.clickable {
                                    onDescriptionChange(ChampDescription.SEXE, Sexe.HOMME.name)
                                }
                            )
                            Text(text = "/", color = Color.Gray, fontFamily = FontFamily.Serif, fontSize = 14.sp)
                            Text(
                                text = stringResource(R.string.femme),
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (uiState.sexe == Sexe.FEMME) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.sexe == Sexe.FEMME) VoyageurColors.NomCaracteristique else Color.Gray,
                                modifier = Modifier.clickable {
                                    onDescriptionChange(ChampDescription.SEXE, Sexe.FEMME.name)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(0.30f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_age),
                                valeur = uiState.age?.toString() ?: "",
                                valueColor = Color.Black,
                                verticalPadding = 0.dp
                            ) { editChamp = ChampDescription.AGE to (uiState.age?.toString() ?: "") }
                        }
                    }

                    // Ligne 3 — Taille + Poids
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp) // Espace C = 6dp
                    ) {
                        Box(modifier = Modifier.weight(0.5f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_taille),
                                valeur = uiState.tailleCm?.let { FormatPhysique.formatTailleCm(it) } ?: "—",
                                valueColor = Color.Black,
                                verticalPadding = 0.dp
                            ) { editChamp = ChampDescription.TAILLE_CM to (uiState.tailleCm?.toString() ?: "") }
                        }
                        Box(modifier = Modifier.weight(0.5f)) {
                            DescriptionLabel(
                                label = stringResource(R.string.description_poids),
                                valeur = uiState.poidsKg?.let { FormatPhysique.formatPoids(it) } ?: "—",
                                valueColor = Color.Black,
                                verticalPadding = 0.dp
                            ) { editChamp = ChampDescription.POIDS_KG to (uiState.poidsKg?.toString() ?: "") }
                        }
                    }
                }

                // Heure de naissance — 20%
                Box(
                    modifier = Modifier
                        .weight(0.20f)
                        .padding(start = 4.dp), // Espace V = 4dp
                    contentAlignment = Alignment.TopCenter
                ) {
                    HeureNaissancePicker(
                        heureCourante = uiState.heureNaissance,
                        onHeureChange = onHeureNaissanceChange,
                        onAideRequise = { onDemanderAide(ChampAffichage.Heure(uiState.heureNaissance)) }
                    )
                }
            }

            // Ligne 4 — Cheveux + Yeux — pleine largeur
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing)
            ) {
                Box(modifier = Modifier.weight(0.5f)) {
                    DescriptionLabel(
                        label = stringResource(R.string.description_cheveux),
                        valeur = uiState.cheveux,
                        valueColor = Color.Black,
                        verticalPadding = 0.dp
                    ) { editChamp = ChampDescription.CHEVEUX to uiState.cheveux }
                }
                Box(modifier = Modifier.weight(0.5f)) {
                    DescriptionLabel(
                        label = stringResource(R.string.description_yeux),
                        valeur = uiState.yeux,
                        valueColor = Color.Black,
                        verticalPadding = 0.dp
                    ) { editChamp = ChampDescription.YEUX to uiState.yeux }
                }
            }

            // ── Signe particulier — pleine largeur ───────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editChamp = ChampDescription.SIGNE_PARTICULIER to uiState.signeParticulier
                    }
                    .padding(bottom = spacing)
            ) {
                Text(
                    text = stringResource(R.string.description_signes_particuliers),
                    color = VoyageurColors.NomCaracteristique,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp
                )
                if (uiState.signeParticulier.isNotBlank()) {
                    Text(
                        text = uiState.signeParticulier,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Beauté — pleine largeur ───────────────────────────────
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxBeaute = 10 + (uiState.beaute - 10).coerceAtLeast(0) + uiState.pointsRestants
                CaracteristiqueRow(
                    nom = stringResource(R.string.aide_beaute_titre),
                    valeur = uiState.beaute,
                    min = 3,
                    max = minOf(16, maxBeaute),
                    valeurDisplay = uiState.beaute.toString(),
                    labelFontFamily = FontFamily.Serif,
                    valueFontFamily = FontFamily.Serif,
                    spacerEnabled = false,
                    onValeurChange = onBeauteChange,
                    onAideRequise = { onDemanderAide(ChampAffichage.Beaute) },
                    modifier = Modifier.wrapContentWidth()
                )
                Text(
                    text = beauteAdjectif,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            HorizontalDivider(color = VoyageurColors.NomCaracteristique.copy(alpha = 0.1f))
        }
    }

    editChamp?.let { (champ, valeur) ->
        var tempValeur by remember {
            mutableStateOf(TextFieldValue(text = valeur, selection = TextRange(0, valeur.length)))
        }
        val focusRequester = remember { FocusRequester() }
        val isNumeric = champ in listOf(ChampDescription.AGE, ChampDescription.TAILLE_CM, ChampDescription.POIDS_KG)
        
        val dialogTitle = when(champ) {
            ChampDescription.TAILLE_CM -> stringResource(R.string.description_taille) + " " + FormatPhysique.formatFourchetteTaille(uiState.caracteristiques.taille, uiState.sexe)
            ChampDescription.POIDS_KG -> stringResource(R.string.description_poids) + " " + FormatPhysique.formatFourchettePoids(uiState.caracteristiques.taille)
            ChampDescription.CHEVEUX -> stringResource(R.string.description_couleur_cheveux)
            ChampDescription.YEUX -> stringResource(R.string.description_couleur_yeux)
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
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val text = tempValeur.text
                    when (champ) {
                        ChampDescription.TAILLE_CM -> text.toIntOrNull()?.let { onTailleCmSaisie(it) }
                        ChampDescription.POIDS_KG -> text.toIntOrNull()?.let { onPoidsSaisi(it) }
                        else -> onDescriptionChange(champ, text)
                    }
                    editChamp = null
                }) { Text(stringResource(R.string.valider)) }
            },
            dismissButton = {
                TextButton(onClick = { editChamp = null }) { Text(stringResource(R.string.annuler)) }
            }
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun DescriptionLabel(
    label: String, 
    valeur: String, 
    modifier: Modifier = Modifier, 
    verticalPadding: androidx.compose.ui.unit.Dp = 1.dp,
    valueColor: Color = VoyageurColors.ValeurCaracteristique,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            color = VoyageurColors.NomCaracteristique,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            lineHeight = 14.sp
        )
        Text(
            text = valeur.ifBlank { "—" },
            color = valueColor,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            lineHeight = 14.sp
        )
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
