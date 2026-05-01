package com.jb.voyageur.feature.caracteristiques

import com.jb.voyageur.core.domain.usecase.ChampCaracteristique

sealed interface ChampAffichage {
    enum class Principale(val domain: ChampCaracteristique) : ChampAffichage {
        TAILLE(ChampCaracteristique.TAILLE),
        APPARENCE(ChampCaracteristique.APPARENCE),
        CONSTITUTION(ChampCaracteristique.CONSTITUTION),
        FORCE(ChampCaracteristique.FORCE),
        AGILITE(ChampCaracteristique.AGILITE),
        DEXTERITE(ChampCaracteristique.DEXTERITE),
        VUE(ChampCaracteristique.VUE),
        OUIE(ChampCaracteristique.OUIE),
        ODO_GOUT(ChampCaracteristique.ODO_GOUT),
        VOLONTE(ChampCaracteristique.VOLONTE),
        INTELLECT(ChampCaracteristique.INTELLECT),
        EMPATHIE(ChampCaracteristique.EMPATHIE),
        REVE(ChampCaracteristique.REVE),
        CHANCE(ChampCaracteristique.CHANCE)
    }

    enum class Derivee : ChampAffichage {
        MELEE, TIR, LANCER, DEROBEE
    }

    enum class Seuil : ChampAffichage {
        VIE, ENDURANCE, SC, SUST, BONUS_DOM, ENCOMBREMENT
    }

    data object Beaute : ChampAffichage
}
