package com.jb.voyageur.feature.caracteristiques.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.model.Lateralite
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.ui.composable.HeureNaissancePicker
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesUiState

@Composable
fun SectionDescription(
    uiState: CaracteristiquesUiState.Success,
    onDescriptionChange: (ChampDescription, String) -> Unit,
    onLateraliteChange: (Lateralite) -> Unit,
    onHeureNaissanceChange: (HeureNaissance) -> Unit,
    modifier: Modifier = Modifier
) {
    var deplie by rememberSaveable { mutableStateOf(true) }

    Column(modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { deplie = !deplie }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.nom.ifBlank { stringResource(R.string.section_description) },
                fontFamily = GoudyAcc,
                fontSize = 20.sp
            )
            Icon(
                imageVector = if (deplie) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = deplie) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(0.7f)) {
                        OutlinedTextField(
                            value = uiState.nom,
                            onValueChange = { onDescriptionChange(ChampDescription.NOM, it) },
                            label = { Text(stringResource(R.string.description_nom)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onDescriptionChange(ChampDescription.SEXE, Sexe.HOMME.name) },
                                enabled = uiState.sexe != Sexe.HOMME
                            ) { Text(stringResource(R.string.homme)) }
                            Button(
                                onClick = { onDescriptionChange(ChampDescription.SEXE, Sexe.FEMME.name) },
                                enabled = uiState.sexe != Sexe.FEMME
                            ) { Text(stringResource(R.string.femme)) }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.age?.toString() ?: "",
                                onValueChange = { onDescriptionChange(ChampDescription.AGE, it) },
                                label = { Text(stringResource(R.string.description_age)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.tailleCm?.toString() ?: "",
                                onValueChange = { onDescriptionChange(ChampDescription.TAILLE_CM, it) },
                                label = { Text(stringResource(R.string.description_taille_cm)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.poidsKg?.toString() ?: "",
                                onValueChange = { onDescriptionChange(ChampDescription.POIDS_KG, it) },
                                label = { Text(stringResource(R.string.description_poids_kg)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.cheveux,
                            onValueChange = { onDescriptionChange(ChampDescription.CHEVEUX, it) },
                            label = { Text(stringResource(R.string.description_cheveux)) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.yeux,
                            onValueChange = { onDescriptionChange(ChampDescription.YEUX, it) },
                            label = { Text(stringResource(R.string.description_yeux)) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.signeParticulier,
                            onValueChange = { onDescriptionChange(ChampDescription.SIGNE_PARTICULIER, it) },
                            label = { Text(stringResource(R.string.description_signe_particulier)) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        Row(
                            Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(stringResource(R.string.lateralite))
                            Button(
                                onClick = { onLateraliteChange(Lateralite.DROITIER) },
                                enabled = uiState.lateralite != Lateralite.DROITIER
                            ) { Text(stringResource(R.string.droitier)) }
                            Button(
                                onClick = { onLateraliteChange(Lateralite.GAUCHER) },
                                enabled = uiState.lateralite != Lateralite.GAUCHER
                            ) { Text(stringResource(R.string.gaucher)) }
                        }
                    }

                    HeureNaissancePicker(
                        heureCourante = uiState.heureNaissance,
                        onHeureChange = onHeureNaissanceChange,
                        modifier = Modifier.weight(0.3f).padding(top = 8.dp)
                    )
                }
            }
        }
        HorizontalDivider()
    }
}
