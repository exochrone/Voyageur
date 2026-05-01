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
)

data class SortAchete(val nom: String)
data class ObjetPossede(val nom: String)
