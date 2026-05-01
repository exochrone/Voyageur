package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.jb.voyageur.core.ui.theme.VoyageurColors

@Composable
fun ParcheminBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        val parchemin = remember { genererTextureParchemin() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Couleur de base ocre/crème
            drawRect(color = VoyageurColors.ParcheminBase)

            // 2. Dégradé radial périphérique (vignette)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        VoyageurColors.ParcheminVignette
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = maxOf(size.width, size.height) * 0.75f
                )
            )

            // 3. Texture fibres (bruit pré-calculé)
            drawImage(parchemin, topLeft = Offset.Zero)
        }

        content()
    }
}

private fun genererTextureParchemin(): ImageBitmap {
    val largeur = 512
    val hauteur = 512
    val bitmap = ImageBitmap(largeur, hauteur)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { alpha = 0.06f }

    val random = kotlin.random.Random(seed = 42)
    repeat(800) {
        val x1 = random.nextFloat() * largeur
        val y1 = random.nextFloat() * hauteur
        val longueur = random.nextFloat() * 40 + 5
        val angle = random.nextFloat() * Math.PI.toFloat()
        val x2 = x1 + longueur * kotlin.math.cos(angle)
        val y2 = y1 + longueur * kotlin.math.sin(angle)

        paint.color = if (random.nextBoolean()) VoyageurColors.FibreFoncee else VoyageurColors.FibreClaire
        canvas.drawLine(Offset(x1, y1), Offset(x2, y2), paint)
    }
    return bitmap
}
