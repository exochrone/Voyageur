package com.jb.voyageur.core.ui.helper

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AideRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val helpTexts = mutableMapOf<String, String>() // élément -> texte_aide

    init {
        loadHelpTexts()
    }

    private fun loadHelpTexts() {
        try {
            val inputStream = context.assets.open("textes_aide.csv")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isBlank()) return@forEach
                    // On gère le cas où le texte contiendrait des points-virgules (normalement entouré de guillemets)
                    // Mais ici on suppose un format simple : catégorie;élément;texte
                    val parts = line.split(";")
                    if (parts.size >= 3) {
                        val element = parts[1].trim()
                        val text = parts[2].trim().removeSurrounding("\"")
                        helpTexts[element.lowercase()] = text
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHelpText(element: String): String? {
        val lower = element.lowercase()
        // Normalisation des apostrophes pour la recherche
        val normalized = lower.replace("'", "’")
        return helpTexts[normalized] ?: helpTexts[lower]
    }
}
