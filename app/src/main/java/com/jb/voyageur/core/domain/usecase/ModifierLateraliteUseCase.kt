package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Lateralite
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierLateraliteUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(voyageurId: Long, lateralite: Lateralite) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        voyageurRepository.sauvegarder(voyageur.copy(lateralite = lateralite))
    }
}
