package com.jb.voyageur.core.domain.model

object CatalogueCompetences {
    val toutes: List<Competence> = listOf(
        // Générales (-4)
        Competence("Bricolage", FamilleCompetence.GENERALE),
        Competence("Chant", FamilleCompetence.GENERALE),
        Competence("Course", FamilleCompetence.GENERALE),
        Competence("Cuisine", FamilleCompetence.GENERALE),
        Competence("Danse", FamilleCompetence.GENERALE),
        Competence("Dessin", FamilleCompetence.GENERALE),
        Competence("Discrétion", FamilleCompetence.GENERALE),
        Competence("Escalade", FamilleCompetence.GENERALE),
        Competence("Saut", FamilleCompetence.GENERALE),
        Competence("Séduction", FamilleCompetence.GENERALE),
        Competence("Vigilance", FamilleCompetence.GENERALE),
        
        // Combat Mêlée (-6) — tronc Corps à corps
        Competence("Corps à corps", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncCorps"),
        Competence("Dague de mêlée", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncCorps"),
        Competence("Esquive", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncCorps"),
        
        // Combat Mêlée (-6) — tronc Armes
        Competence("Épée à une main", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Épée à deux mains", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Hache à une main", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Hache à deux mains", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Masse à une main", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Masse à deux mains", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        Competence("Lance", FamilleCompetence.COMBAT_MELEE, appartientAuTronc = "TroncArmes"),
        
        // Combat Mêlée (-6) — indépendantes
        Competence("Bouclier", FamilleCompetence.COMBAT_MELEE),
        Competence("Fléau", FamilleCompetence.COMBAT_MELEE),
        Competence("Arme d'hast", FamilleCompetence.COMBAT_MELEE),
        
        // Tir et Lancer (-8)
        Competence("Arbalète", FamilleCompetence.TIR_LANCER),
        Competence("Arc", FamilleCompetence.TIR_LANCER),
        Competence("Fronde", FamilleCompetence.TIR_LANCER),
        Competence("Dague de jet", FamilleCompetence.TIR_LANCER),
        Competence("Javelot", FamilleCompetence.TIR_LANCER),
        Competence("Fouet", FamilleCompetence.TIR_LANCER),
        
        // Particulières (-8)
        Competence("Charpenterie", FamilleCompetence.PARTICULIERE),
        Competence("Comédie", FamilleCompetence.PARTICULIERE),
        Competence("Commerce", FamilleCompetence.PARTICULIERE),
        Competence("Équitation", FamilleCompetence.PARTICULIERE),
        Competence("Maçonnerie", FamilleCompetence.PARTICULIERE),
        Competence("Musique", FamilleCompetence.PARTICULIERE),
        Competence("Pickpocket", FamilleCompetence.PARTICULIERE),
        Competence("Survie en extérieur", FamilleCompetence.PARTICULIERE),
        Competence("Survie en désert", FamilleCompetence.PARTICULIERE),
        Competence("Survie en forêt", FamilleCompetence.PARTICULIERE),
        Competence("Survie en glaces", FamilleCompetence.PARTICULIERE),
        Competence("Survie en marais", FamilleCompetence.PARTICULIERE),
        Competence("Survie en montagne", FamilleCompetence.PARTICULIERE),
        Competence("Survie en sous-sol", FamilleCompetence.PARTICULIERE),
        Competence("Travestissement", FamilleCompetence.PARTICULIERE),
        
        // Spécialisées (-11)
        Competence("Acrobatie", FamilleCompetence.SPECIALISEE),
        Competence("Chirurgie", FamilleCompetence.SPECIALISEE),
        Competence("Jeu", FamilleCompetence.SPECIALISEE),
        Competence("Jonglerie", FamilleCompetence.SPECIALISEE),
        Competence("Maroquinerie", FamilleCompetence.SPECIALISEE),
        Competence("Métallurgie", FamilleCompetence.SPECIALISEE),
        Competence("Natation", FamilleCompetence.SPECIALISEE),
        Competence("Navigation", FamilleCompetence.SPECIALISEE),
        Competence("Orfèvrerie", FamilleCompetence.SPECIALISEE),
        Competence("Serrurerie", FamilleCompetence.SPECIALISEE),
        
        // Connaissances (-11)
        Competence("Alchimie", FamilleCompetence.CONNAISSANCE),
        Competence("Astrologie", FamilleCompetence.CONNAISSANCE),
        Competence("Botanique", FamilleCompetence.CONNAISSANCE),
        Competence("Écriture", FamilleCompetence.CONNAISSANCE),
        Competence("Légendes", FamilleCompetence.CONNAISSANCE),
        Competence("Médecine", FamilleCompetence.CONNAISSANCE),
        Competence("Zoologie", FamilleCompetence.CONNAISSANCE),
        
        // Draconic (-11)
        Competence("Oniros", FamilleCompetence.DRACONIC),
        Competence("Hypnos", FamilleCompetence.DRACONIC),
        Competence("Narcos", FamilleCompetence.DRACONIC),
        Competence("Thanatos", FamilleCompetence.DRACONIC)
    )

    val parFamille: Map<FamilleCompetence, List<Competence>> =
        toutes.groupBy { it.famille }

    val SURVIES_RESTRICTIVES = setOf(
        "Survie en désert", "Survie en forêt", "Survie en glaces",
        "Survie en marais", "Survie en montagne", "Survie en sous-sol"
    )
}
