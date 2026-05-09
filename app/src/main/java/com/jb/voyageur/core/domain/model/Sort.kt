package com.jb.voyageur.core.domain.model

data class Sort(
    val nom: String,
    val difficulte: Int?, // Le X dans R-X, null pour "variable"
    val voie: VoieDraconic,
    val description: String // Le libellé complet tel qu'affiché
) {
    val estAnnulationDeMagie: Boolean get() = nom.uppercase().contains("ANNULATION DE MAGIE")

    fun calculerCoutDeBase(): Int {
        if (estAnnulationDeMagie) return 70
        return (difficulte ?: 0) * 10
    }

    fun calculerSupplement(niveauDraconic: Int): Int {
        val diff = difficulte ?: 0
        val seuilNormal = niveauDraconic + 3
        if (diff <= seuilNormal) return 0
        val niveauxEnTrop = diff - seuilNormal
        return niveauxEnTrop * 20
    }

    fun estAccessible(niveauDraconic: Int): Boolean {
        val diff = difficulte ?: 0
        return diff <= niveauDraconic + 5
    }
}
