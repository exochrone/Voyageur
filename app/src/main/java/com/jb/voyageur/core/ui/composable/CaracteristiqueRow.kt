package com.jb.voyageur.core.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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

@Composable
fun CaracteristiqueRow(
    nom: String,
    valeur: Int,
    min: Int = 6,
    max: Int = 15,
    valeurDisplay: String = valeur.toString(),
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

    val valeurFontSize by animateFloatAsState(
        targetValue = if (isDragging) 28f else 22f,
        label = "valeurFontSize"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(nestedScrollConnection),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nom,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
            color = VoyageurColors.NomCaracteristique,
            modifier = Modifier
                .wrapContentWidth()
                .clickable { onAideRequise() }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragAccumulator = 0f
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { _, dragAmount ->
                            dragAccumulator -= dragAmount
                            val increments = (dragAccumulator / dragThresholdPx).toInt()
                            if (increments != 0) {
                                val nouvelleValeur = (currentValeur + increments).coerceIn(currentMin, currentMax)
                                if (nouvelleValeur != currentValeur) {
                                    if (nouvelleValeur == currentMin || nouvelleValeur == currentMax) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onValeurChange(nouvelleValeur)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                dragAccumulator -= increments * dragThresholdPx
                            }
                        }
                    )
                }
        )
        
        Spacer(Modifier.weight(1f))

        Text(
            text = valeurDisplay,
            fontFamily = FontFamily.Serif,
            fontSize = valeurFontSize.sp,
            fontWeight = FontWeight.Bold,
            color = VoyageurColors.ValeurCaracteristique,
            modifier = Modifier
                .padding(start = 8.dp)
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
