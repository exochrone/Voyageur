package com.jb.voyageur.core.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun CompetenceRow(
    nom: String,
    niveauActuel: Int,
    coutCumule: Int,
    borneInf: Int,
    borneSup: Int,
    onNiveauChange: (Int) -> Unit,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var atBorne by remember { mutableStateOf(false) }
    var showSaisieDialog by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }

    val currentValeur by rememberUpdatedState(niveauActuel)
    val currentBorneInf by rememberUpdatedState(borneInf)
    val currentBorneSup by rememberUpdatedState(borneSup)

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.4f else 1f,
        label = "valeurScale"
    )

    val scoreDisplay = if (niveauActuel >= 0) "+$niveauActuel" else niveauActuel.toString()

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marge gauche 10%
        Spacer(Modifier.weight(0.10f))

        // Libellé 50% — drag + tap aide
        Text(
            text = nom,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
            color = VoyageurColors.NomCaracteristique,
            modifier = Modifier
                .weight(0.50f)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downTime = System.currentTimeMillis()
                            dragAccumulator = 0f
                            var longPressTriggered = false
                            var pointer = down

                            while (true) {
                                val elapsed = System.currentTimeMillis() - downTime
                                val event = withTimeoutOrNull((200 - elapsed).coerceAtLeast(0)) {
                                    awaitPointerEvent()
                                }

                                if (event == null) {
                                    if (pointer.pressed) {
                                        longPressTriggered = true
                                        isDragging = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    break
                                }

                                pointer = event.changes.firstOrNull() ?: break

                                if (!pointer.pressed) {
                                    if (System.currentTimeMillis() - downTime < 200) {
                                        onAideRequise()
                                    }
                                    break
                                }

                                val drag = pointer.position - pointer.previousPosition
                                if (drag.getDistance() > 8.dp.toPx()) break
                            }

                            if (longPressTriggered) {
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        if (!change.pressed) break
                                        change.consume()

                                        val dragAmount = change.previousPosition.y - change.position.y
                                        dragAccumulator += dragAmount
                                        val increments = (dragAccumulator / dragThresholdPx).toInt()

                                        if (increments != 0) {
                                            val nouveau = (currentValeur + increments)
                                                .coerceIn(currentBorneInf, currentBorneSup)
                                            if (nouveau != currentValeur) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onNiveauChange(nouveau)
                                                atBorne = false
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                atBorne = true
                                            }
                                            dragAccumulator -= increments * dragThresholdPx
                                        }
                                    }
                                } finally {
                                    isDragging = false
                                    atBorne = false
                                }
                            }
                        }
                    }
                }
        )

        // Score 15% — tap pour saisie manuelle
        Text(
            text = scoreDisplay,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (atBorne) androidx.compose.ui.graphics.Color(0xFFFF0000) else VoyageurColors.ValeurCaracteristique,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(0.15f)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clickable { showSaisieDialog = true }
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

    if (showSaisieDialog) {
        SaisieNumerique(
            titre = nom,
            valeurInitiale = niveauActuel,
            min = borneInf,
            max = borneSup,
            onValider = { onNiveauChange(it); showSaisieDialog = false },
            onDismiss = { showSaisieDialog = false }
        )
    }
}
