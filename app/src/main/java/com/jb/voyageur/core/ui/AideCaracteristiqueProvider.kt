package com.jb.voyageur.core.ui

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
        val (titreRes, descRes) = when (champ) {
            ChampCaracteristique.TAILLE ->
                R.string.aide_taille_titre to R.string.aide_taille_desc
            ChampCaracteristique.CONSTITUTION ->
                R.string.aide_constitution_titre to R.string.aide_constitution_desc
            ChampCaracteristique.FORCE ->
                R.string.aide_force_titre to R.string.aide_force_desc
            ChampCaracteristique.APPARENCE ->
                R.string.aide_apparence_titre to R.string.aide_apparence_desc
            ChampCaracteristique.AGILITE ->
                R.string.aide_agilite_titre to R.string.aide_agilite_desc
            ChampCaracteristique.DEXTERITE ->
                R.string.aide_dexterite_titre to R.string.aide_dexterite_desc
            ChampCaracteristique.VUE ->
                R.string.aide_vue_titre to R.string.aide_vue_desc
            ChampCaracteristique.OUIE ->
                R.string.aide_ouie_titre to R.string.aide_ouie_desc
            ChampCaracteristique.ODO_GOUT ->
                R.string.aide_odogout_titre to R.string.aide_odogout_desc
            ChampCaracteristique.VOLONTE ->
                R.string.aide_volonte_titre to R.string.aide_volonte_desc
            ChampCaracteristique.INTELLECT ->
                R.string.aide_intellect_titre to R.string.aide_intellect_desc
            ChampCaracteristique.EMPATHIE ->
                R.string.aide_empathie_titre to R.string.aide_empathie_desc
            ChampCaracteristique.REVE ->
                R.string.aide_reve_titre to R.string.aide_reve_desc
            ChampCaracteristique.CHANCE ->
                R.string.aide_chance_titre to R.string.aide_chance_desc
        }
        return AideCaracteristique(champ, resources.getString(titreRes), resources.getString(descRes))
    }
}
