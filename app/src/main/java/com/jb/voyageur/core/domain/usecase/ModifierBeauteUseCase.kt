package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierBeauteUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        nouvelleValeur: Int
    ) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val valeurClampee = nouvelleValeur.coerceIn(3, 16)
        voyageurRepository.sauvegarder(voyageur.copy(beaute = valeurClampee))
    }
}
