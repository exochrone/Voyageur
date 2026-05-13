package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.navigation.SequenceEcrans
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.core.ui.theme.VoyageurColors

@Composable
fun BarreNavigationEcran(
    titre: String,
    ecranCourant: EcranCreation,
    afficherSorts: Boolean,
    onNaviguerVers: (EcranCreation) -> Unit,
    modifier: Modifier = Modifier
) {
    val ecranPrecedent = SequenceEcrans.precedent(ecranCourant, afficherSorts)
    val ecranSuivant   = SequenceEcrans.suivant(ecranCourant, afficherSorts)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .draggable(
                state = rememberDraggableState { /* On ne gère pas le mouvement continu */ },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    if (velocity < -500f && ecranSuivant != null) {
                        // Swipe vers la gauche (vitesse négative) -> Suivant
                        onNaviguerVers(ecranSuivant)
                    } else if (velocity > 500f && ecranPrecedent != null) {
                        // Swipe vers la droite (vitesse positive) -> Précédent
                        onNaviguerVers(ecranPrecedent)
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chevron gauche 10%
        Box(
            modifier = Modifier.weight(0.10f),
            contentAlignment = Alignment.Center
        ) {
            if (ecranPrecedent != null) {
                IconButton(
                    onClick = { onNaviguerVers(ecranPrecedent) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = VoyageurColors.NomCaracteristique,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Titre 80%
        Text(
            text       = titre,
            fontFamily = Luminari,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            color      = VoyageurColors.NomCaracteristique,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.weight(0.80f)
        )

        // Chevron droit 10%
        Box(
            modifier = Modifier.weight(0.10f),
            contentAlignment = Alignment.Center
        ) {
            if (ecranSuivant != null) {
                IconButton(
                    onClick = { onNaviguerVers(ecranSuivant) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = VoyageurColors.NomCaracteristique,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
