package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierCompetenceUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        nomCompetence: String,
        niveauBase: Int,
        niveauCible: Int
    ) {
        val valeurClampee = niveauCible.coerceIn(niveauBase, 3)
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val nouvCompetences = voyageur.competences.toMutableMap()
        if (valeurClampee == niveauBase) {
            nouvCompetences.remove(nomCompetence) // base = pas stocké
        } else {
            nouvCompetences[nomCompetence] = valeurClampee
        }
        voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
    }
}
