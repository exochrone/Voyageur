package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.theme.VoyageurColors

@Composable
fun CompetenceRow(
    nom: String,
    niveauActuel: Int,
    coutCumule: Int,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreDisplay = if (niveauActuel >= 0) "+$niveauActuel" else niveauActuel.toString()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAideRequise),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marge gauche 10%
        Spacer(Modifier.weight(0.10f))

        // Libellé 50%
        Text(
            text = nom,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            color = VoyageurColors.NomCaracteristique,
            modifier = Modifier.weight(0.50f)
        )

        // Score 15%
        Text(
            text = scoreDisplay,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VoyageurColors.ValeurCaracteristique,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.15f)
        )

        // Points dépensés 15%
        Text(
            text = if (coutCumule > 0) "$coutCumule" else "",
            fontFamily = FontFamily.Default,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.15f)
        )

        // Marge droite 10%
        Spacer(Modifier.weight(0.10f))
    }
}
