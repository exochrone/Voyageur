package com.jb.voyageur.feature.archetype

import com.jb.voyageur.R

data class CategorieConfig(val titreRes: Int, val competences: List<String>)

val CATEGORIES_ARCHETYPE: List<CategorieConfig> = listOf(
    CategorieConfig(
        titreRes    = R.string.col_generales,
        competences = listOf(
            "Bricolage", "Chant", "Course", "Cuisine", "Danse",
            "Dessin", "Discrétion", "Escalade", "Saut", "Séduction", "Vigilance"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_particulieres,
        competences = listOf(
            "Charpenterie", "Comédie", "Commerce", "Équitation", "Maçonnerie",
            "Musique", "Pickpocket", "Survie en cité", "Survie en extérieur",
            "Survie en désert", "Survie en forêt", "Survie en glaces",
            "Survie en marais", "Survie en montagne", "Survie en sous-sol",
            "Travestissement"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_combat,
        competences = listOf(
            // TroncCorps
            "Corps à corps", "Dague de mêlée", "Esquive",
            // TroncArmes
            "Épée à une main", "Épée à deux mains",
            "Hache à une main", "Hache à deux mains",
            "Masse à une main", "Masse à deux mains",
            "Lance",
            // Indépendantes
            "Bouclier", "Fléau", "Arme d'hast"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_tir,
        competences = listOf("Arbalète", "Arc", "Fronde", "Dague de jet", "Javelot", "Fouet")
    ),
    CategorieConfig(
        titreRes    = R.string.col_specialisees,
        competences = listOf(
            "Acrobatie", "Chirurgie", "Jeu", "Jonglerie", "Maroquinerie",
            "Métallurgie", "Natation", "Navigation", "Orfèvrerie", "Serrurerie"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_connaissances,
        competences = listOf(
            "Alchimie", "Astrologie", "Botanique", "Écriture",
            "Légendes", "Médecine", "Zoologie"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_draconic,
        competences = listOf("Oniros", "Hypnos", "Narcos", "Thanatos")
    )
)
