package com.jb.voyageur.core.ui.helper

import android.content.res.Resources
import com.jb.voyageur.R

data class AideCompetence(
    val titre: String,
    val description: String
)

object AideCompetenceProvider {
    fun pour(nom: String, resources: Resources): AideCompetence {
        val resId = when (nom) {
            "Bricolage" -> R.string.aide_bricolage
            "Corps à corps" -> R.string.aide_corps_a_corps
            "Survie en extérieur" -> R.string.aide_survie_exterieur
            "Oniros" -> R.string.aide_oniros
            else -> R.string.aide_competence_generique
        }
        return AideCompetence(nom, resources.getString(resId))
    }
}
