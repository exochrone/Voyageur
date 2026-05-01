package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.ui.theme.HeuresDraconiques

@Composable
fun HeureNaissancePicker(
    heureCourante: HeureNaissance,
    onHeureChange: (HeureNaissance) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = with(LocalDensity.current) { 40.dp.toPx() }

    Column(
        modifier = modifier
            .pointerInput(heureCourante) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                        while (dragAccumulator < -dragThresholdPx) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onHeureChange(heureCourante.next())
                            dragAccumulator += dragThresholdPx
                        }
                        while (dragAccumulator > dragThresholdPx) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onHeureChange(heureCourante.previous())
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
            fontSize = 48.sp,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = heureCourante.label,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp
        )
    }
}
