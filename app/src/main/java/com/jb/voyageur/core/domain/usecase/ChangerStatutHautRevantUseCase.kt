package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Draconic
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ChangerStatutHautRevantUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(voyageurId: Long, hautRevant: Boolean) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        
        val updated = if (!hautRevant && voyageur.hautRevant) {
            // Passage de Haut-rêvant à Vrai-rêvant
            voyageur.copy(
                hautRevant = false,
                draconic = Draconic(), // Reset à -11 partout
                sorts = emptyList() // Efface les sorts
            )
        } else {
            // Passage de Vrai-rêvant à Haut-rêvant (ou pas de changement)
            voyageur.copy(hautRevant = hautRevant)
        }
        
        voyageurRepository.sauvegarder(updated)
    }
}
