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
        val lignes = assets.open("equipement.csv")
            .bufferedReader()
            .readLines()

        val objetsParCategorie = mutableMapOf<String, MutableList<ObjetEquipement>>()
        val ordreCategories = mutableListOf<String>()

        lignes.forEach { ligneRaw ->
            // Nettoyage du BOM et des espaces
            val ligne = ligneRaw.trim().removePrefix("\uFEFF")
            if (ligne.isBlank()) return@forEach
            
            val parts = ligne.split(";")
            if (parts.size >= 4) {
                val categorie = parts[0].trim()
                val nom = parts[1].trim()
                
                // Ignorer les lignes d'entête (peuvent apparaître plusieurs fois)
                if (categorie.equals("catégorie", ignoreCase = true) || 
                    nom.equals("nom_objet", ignoreCase = true) || 
                    nom.equals("nom complet", ignoreCase = true)) {
                    return@forEach
                }

                val enc = parts[2].trim().replace(",", ".").toFloatOrNull() ?: 0f
                val prix = parts[3].trim().toIntOrNull() ?: 0
                
                // Utiliser une clé normalisée pour le dictionnaire mais garder l'affichage original
                val key = objetsParCategorie.keys.find { it.equals(categorie, ignoreCase = true) } ?: categorie
                
                if (!objetsParCategorie.containsKey(key)) {
                    objetsParCategorie[key] = mutableListOf()
                    ordreCategories.add(key)
                }
                objetsParCategorie[key]?.add(ObjetEquipement(nom, enc, prix))
            }
        }

        return ordreCategories.map { nomCat ->
            CategorieEquipement(nomCat, objetsParCategorie[nomCat] ?: emptyList())
        }
    }
}
