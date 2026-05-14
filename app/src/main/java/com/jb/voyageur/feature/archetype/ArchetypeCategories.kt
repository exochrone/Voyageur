package com.jb.voyageur.feature.archetype

import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.FamilleCompetence

data class CategorieConfig(
    val titreRes: Int,
    val famille: FamilleCompetence,
    val competences: List<String>
)

val CATEGORIES_ARCHETYPE: List<CategorieConfig> = listOf(
    CategorieConfig(
        titreRes    = R.string.col_generales,
        famille     = FamilleCompetence.GENERALE,
        competences = listOf(
            "Bricolage", "Chant", "Course", "Cuisine", "Danse",
            "Dessin", "Discrétion", "Escalade", "Saut", "Séduction", "Vigilance"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_particulieres,
        famille     = FamilleCompetence.PARTICULIERE,
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
        famille     = FamilleCompetence.COMBAT_MELEE,
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
        famille     = FamilleCompetence.TIR_LANCER,
        competences = listOf("Arbalète", "Arc", "Fronde", "Dague de jet", "Javelot", "Fouet")
    ),
    CategorieConfig(
        titreRes    = R.string.col_specialisees,
        famille     = FamilleCompetence.SPECIALISEE,
        competences = listOf(
            "Acrobatie", "Chirurgie", "Jeu", "Jonglerie", "Maroquinerie",
            "Métallurgie", "Natation", "Navigation", "Orfèvrerie", "Serrurerie"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_connaissances,
        famille     = FamilleCompetence.CONNAISSANCE,
        competences = listOf(
            "Alchimie", "Astrologie", "Botanique", "Écriture",
            "Légendes", "Médecine", "Zoologie"
        )
    ),
    CategorieConfig(
        titreRes    = R.string.col_draconic,
        famille     = FamilleCompetence.DRACONIC,
        competences = listOf("Oniros", "Hypnos", "Narcos", "Thanatos")
    )
)
