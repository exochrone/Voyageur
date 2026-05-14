package com.jb.voyageur.core.ui.helper

import android.content.res.Resources
import com.jb.voyageur.R
import com.jb.voyageur.feature.caracteristiques.ChampAffichage
import javax.inject.Inject
import javax.inject.Singleton

data class AideCaracteristique(
    val titre: String,
    val description: String
)

@Singleton
class AideCaracteristiqueProvider @Inject constructor(
    private val aideRepository: AideRepository
) {
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

        val titre = if (champ is ChampAffichage.Heure) champ.heure.label else resources.getString(titreRes)
        
        if (champ is ChampAffichage.Heure) {
            return AideCaracteristique(titre, "")
        }

        // On essaie de trouver dans le CSV via le titre (en majuscules ou tel quel)
        // Attention pour ODO-GOÛT -> ODORAT-GOÛT
        val elementSearch = if (titre == "ODO-GOÛT") "ODORAT-GOÛT" else titre
        
        val aideCsv = aideRepository.getHelpText(elementSearch)
        val description = aideCsv ?: resources.getString(R.string.aide_generique_desc)

        return AideCaracteristique(titre, description)
    }
}
