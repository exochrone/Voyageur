package com.jb.voyageur.feature.aide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.jb.voyageur.core.ui.composable.ParcheminBackground
import com.jb.voyageur.core.ui.theme.VoyageurColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AideScreen() {
    val context = LocalContext.current
    var markdownContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        markdownContent = withContext(Dispatchers.IO) {
            try {
                context.assets.open("aide.md").bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                "Erreur lors du chargement de l'aide."
            }
        }
    }

    ParcheminBackground(modifier = Modifier.fillMaxSize()) {
        if (markdownContent == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VoyageurColors.NomCaracteristique)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                MarkdownText(
                    markdown = markdownContent!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontFamily = FontFamily.Serif
                    )
                )
            }
        }
    }
}
