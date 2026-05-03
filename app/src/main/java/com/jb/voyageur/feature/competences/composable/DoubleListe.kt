package com.jb.voyageur.feature.competences.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.composable.CompetenceRow
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.core.ui.theme.VoyageurColors
import com.jb.voyageur.feature.competences.CompetenceUiItem
import com.jb.voyageur.feature.competences.DoubleListe

@Composable
fun DoubleListeCompetences(
    doubleListe: DoubleListe,
    onAideRequise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // En-têtes des deux colonnes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Approx 4% + 4% margins
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(doubleListe.gauche.famille.labelRes),
                fontFamily = GoudyAcc,
                fontSize = 13.sp,
                color = VoyageurColors.NomCaracteristique,
                modifier = Modifier.weight(0.43f)
            )
            Spacer(Modifier.weight(0.06f))
            Text(
                text = if (doubleListe.droite.items.isNotEmpty())
                    stringResource(doubleListe.droite.famille.labelRes)
                else "",
                fontFamily = GoudyAcc,
                fontSize = 13.sp,
                color = VoyageurColors.NomCaracteristique,
                modifier = Modifier.weight(0.43f)
            )
        }

        HorizontalDivider(
            color = VoyageurColors.NomCaracteristique.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        )

        // Lignes de compétences
        val maxItems = maxOf(doubleListe.gauche.items.size, doubleListe.droite.items.size)

        for (index in 0 until maxItems) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Marge 4% approx
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonne gauche 43%
                Box(modifier = Modifier.weight(0.43f)) {
                    val item = doubleListe.gauche.items.getOrNull(index)
                    if (item != null) {
                        when (item) {
                            is CompetenceUiItem.Individuelle ->
                                CompetenceRow(
                                    nom = item.competence.nom,
                                    niveauActuel = item.niveauActuel,
                                    onAideRequise = { onAideRequise(item.competence.nom) }
                                )
                            is CompetenceUiItem.Separateur ->
                                Text(
                                    text = stringResource(item.labelRes),
                                    fontFamily = GoudyAcc,
                                    fontSize = 13.sp,
                                    color = VoyageurColors.NomCaracteristique,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                        }
                    }
                }

                // Espacement 6%
                Spacer(Modifier.weight(0.06f))

                // Colonne droite 43%
                Box(modifier = Modifier.weight(0.43f)) {
                    val item = doubleListe.droite.items.getOrNull(index)
                    if (item != null) {
                        when (item) {
                            is CompetenceUiItem.Individuelle ->
                                CompetenceRow(
                                    nom = item.competence.nom,
                                    niveauActuel = item.niveauActuel,
                                    onAideRequise = { onAideRequise(item.competence.nom) }
                                )
                            is CompetenceUiItem.Separateur -> { /* Not expected on right for now */ }
                        }
                    }
                }
            }
        }
    }
}
