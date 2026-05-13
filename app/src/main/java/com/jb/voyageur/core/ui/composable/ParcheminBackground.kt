package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.jb.voyageur.core.ui.theme.VoyageurColors
import android.net.Uri

val LocalBackgroundImageUri = staticCompositionLocalOf<Uri?> { null }

@Composable
fun ParcheminBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val imageUri = LocalBackgroundImageUri.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (imageUri == null) Modifier.background(VoyageurColors.ParcheminBase)
                else Modifier // Transparent si une image est présente, le fond est géré par le thème
            )
    ) {
        content()
    }
}
