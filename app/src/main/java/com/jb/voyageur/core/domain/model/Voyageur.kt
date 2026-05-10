package com.jb.voyageur.core.domain.model

data class Voyageur(
    val id: Long = 0L,
    val nom: String = "",
    val sexe: Sexe = Sexe.HOMME,
    val hautRevant: Boolean = false,
    val age: Int? = null,
    val tailleCm: Int? = null,
    val poidsKg: Int? = null,
    val cheveux: String = "",
    val yeux: String = "",
    val signeParticulier: String = "",
    val lateralite: Lateralite = Lateralite.DROITIER,
    val heureNaissance: HeureNaissance = HeureNaissance.VAISSEAU,
    val caracteristiques: Caracteristiques = Caracteristiques(),
    val beaute: Int = 10,
    val competences: Map<String, Int> = emptyMap(),
    val troncCorps: Tronc = Tronc(
        nom = "TroncCorps",
        membres = listOf("Corps à corps", "Dague de mêlée", "Esquive"),
        niveauBase = -6
    ),
    val troncArmes: Tronc = Tronc(
        nom = "TroncArmes",
        membres = listOf(
            "Épée à une main", "Épée à deux mains",
            "Hache à une main", "Hache à deux mains",
            "Masse à une main", "Masse à deux mains",
            "Lance"
        ),
        niveauBase = -6
    ),
    val draconic: Draconic = Draconic(),
    val sorts: List<SortAchete> = emptyList(),
    val equipement: List<ObjetPossede> = emptyList(),
    val fortune: Int = 5000,
    val archetype: Map<String, Int> = emptyMap()
)

enum class Sexe { HOMME, FEMME }
enum class Lateralite { DROITIER, GAUCHER }

enum class HeureNaissance(val symbole: Char, val label: String) {
    VAISSEAU('a', "Vaisseau"),
    SIRENE('b', "Sirène"),
    FAUCON('c', "Faucon"),
    COURONNE('d', "Couronne"),
    DRAGON('e', "Dragon"),
    EPEES('f', "Épées"),
    LYRE('g', "Lyre"),
    SERPENT('h', "Serpent"),
    POISSON_ACROBATE('i', "Poisson Acrobate"),
    ARAIGNEE('j', "Araignée"),
    ROSEAU('k', "Roseau"),
    CHATEAU_DORMANT('l', "Château Dormant");

    fun next(): HeureNaissance = entries[(ordinal + 1) % entries.size]
    fun previous(): HeureNaissance = entries[(ordinal - 1 + entries.size) % entries.size]
}

data class Draconic(
    val oniros: Int = -11,
    val hypnos: Int = -11,
    val narcos: Int = -11,
    val thanatos: Int = -11
) {
    fun niveau(voie: VoieDraconic): Int = when (voie) {
        VoieDraconic.ONIROS   -> oniros
        VoieDraconic.HYPNOS   -> hypnos
        VoieDraconic.NARCOS   -> narcos
        VoieDraconic.THANATOS -> thanatos
    }

    fun avecNiveau(voie: VoieDraconic, niveau: Int): Draconic = when (voie) {
        VoieDraconic.ONIROS   -> copy(oniros = niveau)
        VoieDraconic.HYPNOS   -> copy(hypnos = niveau)
        VoieDraconic.NARCOS   -> copy(narcos = niveau)
        VoieDraconic.THANATOS -> copy(thanatos = niveau)
    }

    fun multiplicateurPour(voie: VoieDraconic): Int =
        if (voie == VoieDraconic.THANATOS) 2
        else 1

    /**
     * Coût total de toutes les voies investies.
     * Thanatos est comptabilisé au double.
     */
    fun pointsTotal(): Int = VoieDraconic.entries.sumOf { voie ->
        CoutCompetence.coutCumuleAvecMultiplicateur(
            niveauBase  = -11,
            niveauCible = niveau(voie),
            multiplicateur = multiplicateurPour(voie)
        )
    }
}

enum class VoieDraconic { ONIROS, HYPNOS, NARCOS, THANATOS }

data class SortAchete(
    val nom: String,
    val voie: VoieDraconic,
    val coutPaye: Int
)

data class ObjetPossede(
    val nom: String,
    val quantite: Int = 1,
    val prixUnitaire: Int,
    val encombrementUnitaire: Float
) {
    val prixTotal: Int get() = prixUnitaire * quantite
    val encombrementTotal: Float get() = encombrementUnitaire * quantite
}
