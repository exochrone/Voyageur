package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.CatalogueCompetences
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierSurvieSpecifiqueUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        nomSurvie: String,
        niveauCible: Int
    ) {
        require(nomSurvie in CatalogueCompetences.SURVIES_SPECIFIQUES) {
            "$nomSurvie n'est pas une survie spécifique"
        }
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val survieExterieur = voyageur.competences["Survie en extérieur"] ?: -8

        // Plafond = niveau de Survie en extérieur si celle-ci est < 0
        val plafond = if (survieExterieur < 0) survieExterieur else 3
        val valeurClampee = niveauCible.coerceIn(-8, plafond)

        val nouvCompetences = voyageur.competences.toMutableMap()
        if (valeurClampee == -8) {
            nouvCompetences.remove(nomSurvie)
        } else {
            nouvCompetences[nomSurvie] = valeurClampee
        }
        voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
    }
}
