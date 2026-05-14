package com.jb.voyageur.core.ui.helper

import android.content.res.Resources
import com.jb.voyageur.R
import javax.inject.Inject
import javax.inject.Singleton

data class AideCompetence(
    val titre: String,
    val description: String
)

@Singleton
class AideCompetenceProvider @Inject constructor(
    private val aideRepository: AideRepository
) {
    fun pour(nom: String, resources: Resources): AideCompetence {
        val aideCsv = aideRepository.getHelpText(nom)
            ?: aideRepository.getHelpText("Voie d’$nom")
            ?: aideRepository.getHelpText("Voie de $nom")

        val description = aideCsv ?: resources.getString(R.string.aide_competence_generique)
        return AideCompetence(nom, description)
    }
}
