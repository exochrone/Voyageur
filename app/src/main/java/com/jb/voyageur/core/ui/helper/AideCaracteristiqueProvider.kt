package com.jb.voyageur.core.ui.helper

import android.content.res.Resources
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.usecase.ChampCaracteristique

data class AideCaracteristique(
    val champ: ChampCaracteristique,
    val titre: String,
    val description: String
)

object AideCaracteristiqueProvider {
    fun pour(champ: ChampCaracteristique, resources: Resources): AideCaracteristique {
        val titreRes = when (champ) {
            ChampCaracteristique.TAILLE -> R.string.aide_taille_titre
            ChampCaracteristique.CONSTITUTION -> R.string.aide_constitution_titre
            ChampCaracteristique.FORCE -> R.string.aide_force_titre
            ChampCaracteristique.APPARENCE -> R.string.aide_apparence_titre
            ChampCaracteristique.AGILITE -> R.string.aide_agilite_titre
            ChampCaracteristique.DEXTERITE -> R.string.aide_dexterite_titre
            ChampCaracteristique.VUE -> R.string.aide_vue_titre
            ChampCaracteristique.OUIE -> R.string.aide_ouie_titre
            ChampCaracteristique.ODO_GOUT -> R.string.aide_odogout_titre
            ChampCaracteristique.VOLONTE -> R.string.aide_volonte_titre
            ChampCaracteristique.INTELLECT -> R.string.aide_intellect_titre
            ChampCaracteristique.EMPATHIE -> R.string.aide_empathie_titre
            ChampCaracteristique.REVE -> R.string.aide_reve_titre
            ChampCaracteristique.CHANCE -> R.string.aide_chance_titre
            ChampCaracteristique.MELEE -> R.string.aide_melee_titre
            ChampCaracteristique.TIR -> R.string.aide_tir_titre
            ChampCaracteristique.LANCER -> R.string.aide_lancer_titre
            ChampCaracteristique.DEROBEE -> R.string.aide_derobee_titre
            ChampCaracteristique.VIE -> R.string.aide_vie_titre
            ChampCaracteristique.ENDURANCE -> R.string.aide_endurance_titre
            ChampCaracteristique.SC -> R.string.aide_sc_titre
            ChampCaracteristique.SUST -> R.string.aide_sust_titre
            ChampCaracteristique.BONUS_DOM -> R.string.aide_bonus_dom_titre
            ChampCaracteristique.ENCOMBREMENT -> R.string.aide_encombrement_titre
        }

        val descRes = when (champ) {
            ChampCaracteristique.TAILLE -> R.string.aide_taille_desc
            ChampCaracteristique.CONSTITUTION -> R.string.aide_constitution_desc
            ChampCaracteristique.FORCE -> R.string.aide_force_desc
            ChampCaracteristique.APPARENCE -> R.string.aide_apparence_desc
            ChampCaracteristique.AGILITE -> R.string.aide_agilite_desc
            ChampCaracteristique.DEXTERITE -> R.string.aide_dexterite_desc
            ChampCaracteristique.VUE -> R.string.aide_vue_desc
            ChampCaracteristique.OUIE -> R.string.aide_ouie_desc
            ChampCaracteristique.ODO_GOUT -> R.string.aide_odogout_desc
            ChampCaracteristique.VOLONTE -> R.string.aide_volonte_desc
            ChampCaracteristique.INTELLECT -> R.string.aide_intellect_desc
            ChampCaracteristique.EMPATHIE -> R.string.aide_empathie_desc
            ChampCaracteristique.REVE -> R.string.aide_reve_desc
            ChampCaracteristique.CHANCE -> R.string.aide_chance_desc
            ChampCaracteristique.MELEE -> R.string.aide_melee_desc
            ChampCaracteristique.TIR -> R.string.aide_tir_desc
            ChampCaracteristique.LANCER -> R.string.aide_lancer_desc
            ChampCaracteristique.DEROBEE -> R.string.aide_derobee_desc
            ChampCaracteristique.VIE -> R.string.aide_vie_desc
            ChampCaracteristique.ENDURANCE -> R.string.aide_endurance_desc
            ChampCaracteristique.SC -> R.string.aide_sc_desc
            ChampCaracteristique.SUST -> R.string.aide_sust_desc
            ChampCaracteristique.BONUS_DOM -> R.string.aide_bonus_dom_desc
            ChampCaracteristique.ENCOMBREMENT -> R.string.aide_encombrement_desc
        }

        return AideCaracteristique(
            champ,
            resources.getString(titreRes),
            resources.getString(descRes)
        )
    }
}
