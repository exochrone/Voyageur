package com.jb.voyageur.core.domain.model

data class ArchetypeCompetence(
    val nomCompetence: String,
    val niveau: Int?             // null = non attribué ("-"), 0..11 = attribué
)

data class Archetype(
    val niveaux: Map<String, Int> = emptyMap()   // nom → niveau attribué
    // Compétences absentes de la map = non attribuées ("-")
) {
    fun estComplet(): Boolean {
        val totalAttribue = NIVEAUX_DISPONIBLES
            .filterKeys { it > 0 }
            .values.sum()  // = 56
        return niveaux.size >= totalAttribue
    }

    companion object {
        // Table de répartition immuable
        val NIVEAUX_DISPONIBLES: Map<Int, Int> = mapOf(
            11 to 1, 10 to 1, 9 to 2, 8 to 3,
            7 to 4, 6 to 5, 5 to 6, 4 to 7,
            3 to 8, 2 to 9, 1 to 10, 0 to Int.MAX_VALUE
        )
    }
}
