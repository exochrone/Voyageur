package com.jb.voyageur.core.domain.model

import android.content.res.AssetManager

data class CategorieEquipement(
    val nom: String,
    val objets: List<ObjetEquipement>
)

object CatalogueEquipement {

    private var _categories: List<CategorieEquipement>? = null

    fun charger(assets: AssetManager): List<CategorieEquipement> {
        return _categories ?: parse(assets).also { _categories = it }
    }

    private fun parse(assets: AssetManager): List<CategorieEquipement> {
        val lignes = assets.open("ListeEquipement.txt")
            .bufferedReader()
            .readLines()

        val categories = mutableListOf<CategorieEquipement>()
        var categorieEnCours: String? = null
        val objetsEnCours = mutableListOf<ObjetEquipement>()

        for (ligne in lignes) {
            when {
                ligne.isBlank() || ligne.startsWith(";") -> continue

                ligne.startsWith("#") -> {
                    // Sauvegarder la catégorie précédente
                    categorieEnCours?.let {
                        categories.add(CategorieEquipement(it, objetsEnCours.toList()))
                    }
                    categorieEnCours = ligne.removePrefix("#").trim()
                    objetsEnCours.clear()
                }

                else -> {
                    val parts = ligne.split("\\")
                    if (parts.size >= 3) {
                        val nom  = parts[0].trim()
                        val enc  = parts[1].trim().replace(",", ".").toFloatOrNull() ?: 0f
                        val prix = parts[2].trim().toIntOrNull() ?: 0
                        objetsEnCours.add(ObjetEquipement(nom, enc, prix))
                    }
                }
            }
        }
        // Dernière catégorie
        categorieEnCours?.let {
            categories.add(CategorieEquipement(it, objetsEnCours.toList()))
        }

        return categories
    }
}
