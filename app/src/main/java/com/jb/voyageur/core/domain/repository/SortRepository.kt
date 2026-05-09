package com.jb.voyageur.core.domain.repository

import android.content.Context
import com.jb.voyageur.core.domain.model.Sort
import com.jb.voyageur.core.domain.model.VoieDraconic
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SortRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache = mutableMapOf<VoieDraconic, List<Sort>>()

    fun chargerSorts(voie: VoieDraconic): List<Sort> {
        return cache.getOrPut(voie) {
            try {
                val fileName = voie.name.lowercase() + ".txt"
                context.assets.open(fileName).bufferedReader().useLines { lines ->
                    lines
                        .filter { it.isNotBlank() && !it.startsWith(";") }
                        .map { parseLine(it, voie) }
                        .toList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parseLine(line: String, voie: VoieDraconic): Sort {
        val rMatch = Regex("R-(\\d+)").find(line)
        val difficulte = rMatch?.groupValues?.get(1)?.toInt()
        
        // On prend tout ce qui précède le R-X ou "variable" pour le nom simplifié si besoin
        // Mais la consigne dit "tels qu'ils apparaissent dans le fichier"
        return Sort(
            nom = line.substringBefore(" R-").substringBefore(" variable").trim(),
            difficulte = difficulte,
            voie = voie,
            description = line.trim()
        )
    }
}
