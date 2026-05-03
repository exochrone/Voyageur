package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.VoieDraconic
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierDraconicUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        voie: VoieDraconic,
        niveauCible: Int
    ) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val valeurClampee = niveauCible.coerceIn(-11, 3)
        val nouvDraconic = voyageur.draconic.avecNiveau(voie, valeurClampee)
        voyageurRepository.sauvegarder(voyageur.copy(draconic = nouvDraconic))
    }
}
