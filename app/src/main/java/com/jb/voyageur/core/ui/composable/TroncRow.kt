package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.CatalogueCompetences
import com.jb.voyageur.core.domain.model.CoutCompetence
import com.jb.voyageur.core.domain.model.Tronc
import com.jb.voyageur.core.ui.theme.GoudyAcc

@Composable
fun TroncRow(
    tronc: Tronc,
    label: String,
    onNiveauCommunChange: (Int) -> Unit,
    onMembreChange: (String, Int) -> Unit,
    onAideRequise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = label,
            fontFamily = GoudyAcc,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        if (!tronc.estSepare) {
            CompetenceRow(
                nom = tronc.membres.joinToString(" / "),
                niveauActuel = tronc.niveauCommun,
                niveauBase = tronc.niveauBase,
                coutCumule = CoutCompetence.coutCumule(tronc.niveauBase, tronc.niveauCommun),
                onNiveauChange = onNiveauCommunChange,
                onAideRequise = { onAideRequise(tronc.membres.first()) }
            )
        } else {
            tronc.membres.forEach { membre ->
                val niveauIndividuel = tronc.niveauxIndividuels[membre] ?: 0
                CompetenceRow(
                    nom = membre,
                    niveauActuel = niveauIndividuel,
                    niveauBase = 0,
                    coutCumule = CoutCompetence.coutCumule(0, niveauIndividuel),
                    onNiveauChange = { onMembreChange(membre, it) },
                    onAideRequise = { onAideRequise(membre) }
                )
            }
        }
    }
}
