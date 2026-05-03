package com.jb.voyageur.core.ui.helper

import android.content.res.Resources
import com.jb.voyageur.R
import com.jb.voyageur.feature.caracteristiques.ChampAffichage

data class AideCaracteristique(
    val titre: String,
    val description: String
)

object AideCaracteristiqueProvider {
    fun pour(champ: ChampAffichage, resources: Resources): AideCaracteristique {
        val titreRes = when (champ) {
            ChampAffichage.Principale.TAILLE -> R.string.carac_taille
            ChampAffichage.Principale.CONSTITUTION -> R.string.carac_constitution
            ChampAffichage.Principale.FORCE -> R.string.carac_force
            ChampAffichage.Principale.APPARENCE -> R.string.carac_apparence
            ChampAffichage.Principale.AGILITE -> R.string.carac_agilite
            ChampAffichage.Principale.DEXTERITE -> R.string.carac_dexterite
            ChampAffichage.Principale.VUE -> R.string.carac_vue
            ChampAffichage.Principale.OUIE -> R.string.carac_ouie
            ChampAffichage.Principale.ODO_GOUT -> R.string.carac_odogout
            ChampAffichage.Principale.VOLONTE -> R.string.carac_volonte
            ChampAffichage.Principale.INTELLECT -> R.string.carac_intellect
            ChampAffichage.Principale.EMPATHIE -> R.string.carac_empathie
            ChampAffichage.Principale.REVE -> R.string.carac_reve
            ChampAffichage.Principale.CHANCE -> R.string.carac_chance
            
            ChampAffichage.Derivee.MELEE -> R.string.derivee_melee
            ChampAffichage.Derivee.TIR -> R.string.derivee_tir
            ChampAffichage.Derivee.LANCER -> R.string.derivee_lancer
            ChampAffichage.Derivee.DEROBEE -> R.string.derivee_derobee
            
            ChampAffichage.Seuil.VIE -> R.string.seuil_vie
            ChampAffichage.Seuil.ENDURANCE -> R.string.seuil_endurance
            ChampAffichage.Seuil.SC -> R.string.seuil_sc
            ChampAffichage.Seuil.SUST -> R.string.seuil_sust
            ChampAffichage.Seuil.BONUS_DOM -> R.string.seuil_bonus_dom
            ChampAffichage.Seuil.ENCOMBREMENT -> R.string.seuil_encombrement
                
            ChampAffichage.Beaute -> R.string.aide_beaute_titre
            is ChampAffichage.Heure -> R.string.heure_naissance
        }

        val descRes = when (champ) {
            ChampAffichage.Principale.TAILLE -> R.string.aide_taille_desc
            ChampAffichage.Principale.CONSTITUTION -> R.string.aide_constitution_desc
            ChampAffichage.Principale.FORCE -> R.string.aide_force_desc
            ChampAffichage.Principale.APPARENCE -> R.string.aide_apparence_desc
            ChampAffichage.Principale.AGILITE -> R.string.aide_agilite_desc
            ChampAffichage.Principale.DEXTERITE -> R.string.aide_dexterite_desc
            ChampAffichage.Principale.VUE -> R.string.aide_vue_desc
            ChampAffichage.Principale.OUIE -> R.string.aide_ouie_desc
            ChampAffichage.Principale.ODO_GOUT -> R.string.aide_odogout_desc
            ChampAffichage.Principale.VOLONTE -> R.string.aide_volonte_desc
            ChampAffichage.Principale.INTELLECT -> R.string.aide_intellect_desc
            ChampAffichage.Principale.EMPATHIE -> R.string.aide_empathie_desc
            ChampAffichage.Principale.REVE -> R.string.aide_reve_desc
            ChampAffichage.Principale.CHANCE -> R.string.aide_chance_desc
            
            ChampAffichage.Derivee.MELEE -> R.string.aide_melee_desc
            ChampAffichage.Derivee.TIR -> R.string.aide_tir_desc
            ChampAffichage.Derivee.LANCER -> R.string.aide_lancer_desc
            ChampAffichage.Derivee.DEROBEE -> R.string.aide_derobee_desc
            
            ChampAffichage.Seuil.VIE -> R.string.aide_vie_desc
            ChampAffichage.Seuil.ENDURANCE -> R.string.aide_endurance_desc
            ChampAffichage.Seuil.SC -> R.string.aide_sc_desc
            ChampAffichage.Seuil.SUST -> R.string.aide_sust_desc
            ChampAffichage.Seuil.BONUS_DOM -> R.string.aide_bonus_dom_desc
            ChampAffichage.Seuil.ENCOMBREMENT -> R.string.aide_encombrement_desc
                
            ChampAffichage.Beaute -> R.string.aide_beaute_desc
            is ChampAffichage.Heure -> R.string.aide_generique_desc
        }

        val titre = if (champ is ChampAffichage.Heure) champ.heure.label else resources.getString(titreRes)
        val description = if (champ is ChampAffichage.Heure) resources.getString(R.string.aide_generique_desc) else resources.getString(descRes)

        return AideCaracteristique(titre, description)
    }
}
