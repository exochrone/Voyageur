package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierHeureNaissanceUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(voyageurId: Long, heure: HeureNaissance) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        voyageurRepository.sauvegarder(voyageur.copy(heureNaissance = heure))
    }
}
