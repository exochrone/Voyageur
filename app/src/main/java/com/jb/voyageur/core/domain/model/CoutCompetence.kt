package com.jb.voyageur.core.domain.model

object CoutCompetence {
    /**
     * Coût en points d'XP pour un seul niveau,
     * déterminé par la tranche dans laquelle tombe le niveau cible.
     */
    fun coutUnNiveau(niveauCible: Int): Int = when (niveauCible) {
        in -10..-8 -> 5
        in -7..-4 -> 10
        in -3..0 -> 15
        in 1..3 -> 20
        else -> 0 // niveau de base (ex: -11 pour draconic) = 0
    }

    /**
     * Coût cumulatif total pour aller de [niveauBase] (exclu) à [niveauCible] (inclus).
     * Retourne 0 si niveauCible <= niveauBase.
     */
    fun coutCumule(niveauBase: Int, niveauCible: Int): Int {
        if (niveauCible <= niveauBase) return 0
        return (niveauBase + 1..niveauCible).sumOf { coutUnNiveau(it) }
    }

    /**
     * Coût cumulé avec multiplicateur.
     * Pour Thanatos : multiplicateur = 2.
     * Pour toutes les autres voies et compétences : multiplicateur = 1.
     */
    fun coutCumuleAvecMultiplicateur(
        niveauBase: Int,
        niveauCible: Int,
        multiplicateur: Int = 1
    ): Int = coutCumule(niveauBase, niveauCible) * multiplicateur

    const val MULTIPLICATEUR_THANATOS = 2
}
