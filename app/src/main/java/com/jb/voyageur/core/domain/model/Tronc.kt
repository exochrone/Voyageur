package com.jb.voyageur.core.domain.model

data class Tronc(
    val nom: String,
    val membres: List<String>,
    val niveauBase: Int,
    val niveauCommun: Int = niveauBase,
    val niveauxIndividuels: Map<String, Int> = emptyMap()
) {
    val estSepare: Boolean get() = niveauCommun >= 0

    fun niveauPour(membre: String): Int =
        if (estSepare) niveauxIndividuels[membre] ?: 0
        else niveauCommun

    fun coutTotal(): Int {
        val coutCommun = CoutCompetence.coutCumule(niveauBase, niveauCommun)
        val coutIndividuels = niveauxIndividuels.values.sumOf { n ->
            CoutCompetence.coutCumule(0, n)
        }
        return coutCommun + coutIndividuels
    }
}
