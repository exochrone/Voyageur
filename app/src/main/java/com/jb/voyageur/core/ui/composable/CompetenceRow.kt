package com.jb.voyageur.core.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun CompetenceRow(
    nom: String,
    niveauActuel: Int,
    niveauBase: Int,
    coutCumule: Int,
    estBloquee: Boolean = false,
    onNiveauChange: (Int) -> Unit,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var showSaisieDialog by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.4f else 1f,
        label = "valeurScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .pointerInput(niveauActuel, niveauBase, estBloquee) {
                    if (estBloquee) return@pointerInput
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
                                            val nouveau = (niveauActuel + increments).coerceIn(niveauBase, 3)
                                            if (nouveau != niveauActuel) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onNiveauChange(nouveau)
                                            } else {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            dragAccumulator -= increments * dragThresholdPx
                                        }
                                    }
                                } finally {
                                    isDragging = false
                                }
                            }
                        }
                    }
                }
        ) {
            Text(
                text = nom,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
                color = if (estBloquee) VoyageurColors.NomCaracteristique.copy(alpha = 0.4f) else VoyageurColors.NomCaracteristique
            )
            if (coutCumule > 0) {
                Text(
                    text = stringResource(R.string.cout_pts, coutCumule),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (niveauActuel >= 0) "+$niveauActuel" else niveauActuel.toString(),
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (estBloquee) VoyageurColors.ValeurCaracteristique.copy(alpha = 0.4f) else VoyageurColors.ValeurCaracteristique,
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .padding(start = 8.dp)
                    .clickable(enabled = !estBloquee) { showSaisieDialog = true }
            )
            if (estBloquee) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = VoyageurColors.ValeurCaracteristique.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp).padding(start = 4.dp)
                )
            }
        }
    }

    if (showSaisieDialog) {
        SaisieNumerique(
            titre = nom,
            valeurInitiale = niveauActuel,
            min = niveauBase,
            max = 3,
            onValider = { onNiveauChange(it); showSaisieDialog = false },
            onDismiss = { showSaisieDialog = false }
        )
    }
}
