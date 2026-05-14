package com.jb.voyageur.core.ui.composable

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.ui.theme.HeuresDraconiques
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun HeureNaissancePicker(
    heureCourante: HeureNaissance,
    onHeureChange: (HeureNaissance) -> Unit,
    onAideRequise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentHeure by rememberUpdatedState(heureCourante)
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = with(LocalDensity.current) { 20.dp.toPx() }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    dragAccumulator = 0f
                    var longPressTriggered = false

                    while (true) {
                        val elapsed = System.currentTimeMillis() - downTime
                        val event = withTimeoutOrNull((200 - elapsed).coerceAtLeast(0)) {
                            awaitPointerEvent()
                        }

                        if (event == null) {
                            // 200ms écoulées sans mouvement → activer le drag
                            longPressTriggered = true
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                            break
                        }

                        val pointer = event.changes.firstOrNull() ?: break
                        if (!pointer.pressed) {
                            // Doigt levé avant 200ms → tap simple → aide
                            if (System.currentTimeMillis() - downTime < 200) {
                                onAideRequise()
                            }
                            break
                        }

                        // Mouvement détecté avant 200ms → laisser scroller (ou interrompre ici)
                        val drag = pointer.position - pointer.previousPosition
                        if (drag.getDistance() > 8.dp.toPx()) break
                    }

                    if (longPressTriggered) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            change.consume()

                            val dragAmount = change.position.x - change.previousPosition.x
                            dragAccumulator += dragAmount
                            
                            while (dragAccumulator < -dragThresholdPx) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onHeureChange(currentHeure.next())
                                dragAccumulator += dragThresholdPx
                            }
                            while (dragAccumulator > dragThresholdPx) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onHeureChange(currentHeure.previous())
                                dragAccumulator -= dragThresholdPx
                            }
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = heureCourante.symbole.toString(),
            fontFamily = HeuresDraconiques,
            fontSize = 64.sp,
            color = androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier.padding(8.dp)
        )
    }
}
