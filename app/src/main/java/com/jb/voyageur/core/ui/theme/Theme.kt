package com.jb.voyageur.core.ui.theme

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.LocalBackgroundImageUri

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun VoyageurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    backgroundImageUri: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // On rend le fond transparent pour voir l'image
    colorScheme = colorScheme.copy(
        background = Color.Transparent,
        surface = colorScheme.surface.copy(alpha = 0.7f),
        surfaceVariant = colorScheme.surfaceVariant.copy(alpha = 0.7f),
    )

    val backgroundModel = backgroundImageUri ?: R.drawable.fond
    val uri = backgroundImageUri?.let { Uri.parse(it) } ?: Uri.parse("android.resource://${context.packageName}/${R.drawable.fond}")

    CompositionLocalProvider(LocalBackgroundImageUri provides uri) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = backgroundModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    content()
                }
            }
        )
    }
}
