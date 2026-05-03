package com.jb.voyageur.core.domain.model

import com.jb.voyageur.R

data class Competence(
    val nom: String,
    val famille: FamilleCompetence,
    val appartientAuTronc: String? = null // nom du tronc, null si indépendante
) {
    val niveauBase: Int get() = famille.base
}

enum class FamilleCompetence(val base: Int, val labelRes: Int, val titreColonneRes: Int) {
    GENERALE(-4, R.string.menu_competences_generales, R.string.col_generales),
    PARTICULIERE(-8, R.string.menu_competences_particulieres, R.string.col_particulieres),
    COMBAT_MELEE(-6, R.string.menu_competences_combat, R.string.col_combat),
    TIR_LANCER(-8, R.string.menu_competences_tir, R.string.col_tir),
    SPECIALISEE(-11, R.string.menu_competences_specialisees, R.string.col_specialisees),
    CONNAISSANCE(-11, R.string.menu_competences_connaissances, R.string.col_connaissances),
    DRACONIC(-11, R.string.menu_competences_draconic, R.string.col_draconic)
}
