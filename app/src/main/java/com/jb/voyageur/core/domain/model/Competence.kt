package com.jb.voyageur.core.domain.model

import com.jb.voyageur.R

data class Competence(
    val nom: String,
    val famille: FamilleCompetence,
    val appartientAuTronc: String? = null // nom du tronc, null si indépendante
) {
    val niveauBase: Int get() = famille.base
}

enum class FamilleCompetence(val base: Int, val labelRes: Int) {
    GENERALE(-4, R.string.menu_competences_generales),
    COMBAT_MELEE(-6, R.string.menu_competences_combat),
    TIR_LANCER(-8, R.string.menu_competences_tir),
    PARTICULIERE(-8, R.string.menu_competences_particulieres),
    SPECIALISEE(-11, R.string.menu_competences_specialisees),
    CONNAISSANCE(-11, R.string.menu_competences_connaissances),
    DRACONIC(-11, R.string.menu_competences_draconic)
}
