package com.jb.voyageur.feature.caracteristiques

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jb.voyageur.core.ui.composable.ParcheminBackground

@Composable
fun CaracteristiquesScreen(
    voyageurId: Long
) {
    ParcheminBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Écran Caractéristiques - ID: $voyageurId")
        }
    }
}
