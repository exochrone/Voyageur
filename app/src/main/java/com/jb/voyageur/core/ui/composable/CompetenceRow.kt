package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.theme.VoyageurColors

@Composable
fun CompetenceRow(
    nom: String,
    niveauActuel: Int,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scoreDisplay = if (niveauActuel >= 0) "+$niveauActuel" else niveauActuel.toString()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nom,
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp,
            color = VoyageurColors.NomCaracteristique,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onAideRequise)
        )
        Text(
            text = scoreDisplay,
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = VoyageurColors.ValeurCaracteristique,
            modifier = Modifier.wrapContentWidth()
        )
    }
}
