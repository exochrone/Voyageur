package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import android.view.HapticFeedbackConstants
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.ui.theme.HeuresDraconiques

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
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount ->
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
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = heureCourante.symbole.toString(),
            fontFamily = HeuresDraconiques,
            fontSize = 64.sp,
            color = androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onAideRequise() }
        )
    }
}
