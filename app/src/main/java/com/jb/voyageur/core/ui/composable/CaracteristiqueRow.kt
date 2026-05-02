package com.jb.voyageur.core.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun CaracteristiqueRow(
    nom: String,
    valeur: Int,
    min: Int = 6,
    max: Int = 15,
    valeurDisplay: String = valeur.toString(),
    labelFontFamily: FontFamily = FontFamily.Serif,
    valueFontFamily: FontFamily = FontFamily.Serif,
    labelPaddingStart: androidx.compose.ui.unit.Dp = 0.dp,
    valuePaddingEnd: androidx.compose.ui.unit.Dp = 0.dp,
    spacerEnabled: Boolean = true,
    onValeurChange: (Int) -> Unit,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var showSaisieDialog by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }

    val currentValeur by rememberUpdatedState(valeur)
    val currentMin by rememberUpdatedState(min)
    val currentMax by rememberUpdatedState(max)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (isDragging) available else Offset.Zero
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.4f else 1f,
        label = "valeurScale"
    )

    Row(
        modifier = modifier
            .then(if (spacerEnabled) Modifier.fillMaxWidth() else Modifier)
            .nestedScroll(nestedScrollConnection),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nom,
            fontFamily = labelFontFamily,
            fontSize = 16.sp,
            fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
            color = VoyageurColors.NomCaracteristique,
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = labelPaddingStart)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Attendre le premier contact
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downTime = System.currentTimeMillis()
                            dragAccumulator = 0f

                            var longPressTriggered = false
                            var pointer = down

                            // Attendre 200ms ou un mouvement
                            while (true) {
                                val elapsed = System.currentTimeMillis() - downTime
                                val event = withTimeoutOrNull((200 - elapsed).coerceAtLeast(0)) {
                                    awaitPointerEvent()
                                }

                                if (event == null) {
                                    // 200ms écoulées sans mouvement → activer le drag seulement si modifiable
                                    if (pointer.pressed && currentMin < currentMax) {
                                        longPressTriggered = true
                                        isDragging = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    break
                                }

                                pointer = event.changes.firstOrNull() ?: break

                                if (!pointer.pressed) {
                                    // Doigt levé avant 200ms → tap simple → aide
                                    if (System.currentTimeMillis() - downTime < 200) {
                                        onAideRequise()
                                    }
                                    break
                                }

                                // Mouvement détecté avant 200ms → laisser scroller
                                val drag = pointer.position - pointer.previousPosition
                                if (drag.getDistance() > 8.dp.toPx()) break
                            }

                            if (longPressTriggered) {
                                try {
                                    // Phase drag active
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        if (!change.pressed) break
                                        change.consume()

                                        val dragAmount = change.previousPosition.y - change.position.y
                                        dragAccumulator += dragAmount
                                        val increments = (dragAccumulator / dragThresholdPx).toInt()
                                        if (increments != 0) {
                                            val nouvelleValeur = (currentValeur + increments)
                                                .coerceIn(currentMin, currentMax)
                                            if (nouvelleValeur != currentValeur) {
                                                val feedbackType = if (nouvelleValeur == currentMin || nouvelleValeur == currentMax)
                                                    HapticFeedbackType.LongPress
                                                else
                                                    HapticFeedbackType.TextHandleMove
                                                haptic.performHapticFeedback(feedbackType)
                                                onValeurChange(nouvelleValeur)
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
        )
        
        if (spacerEnabled) {
            Spacer(Modifier.weight(1f))
        }

        Text(
            text = valeurDisplay,
            fontFamily = valueFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = VoyageurColors.ValeurCaracteristique,
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .padding(start = 8.dp, end = valuePaddingEnd)
                .clickable { if (currentMin != currentMax) showSaisieDialog = true }
        )
    }

    if (showSaisieDialog) {
        SaisieNumerique(
            valeurInitiale = valeur,
            min = min,
            max = max,
            onValider = { nouvelleValeur ->
                onValeurChange(nouvelleValeur)
                showSaisieDialog = false
            },
            onDismiss = { showSaisieDialog = false }
        )
    }
}
