package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class RembourserObjetUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(voyageurId: Long, nomObjet: String) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return

        val equipementMaj = voyageur.equipement.toMutableList()
        val index = equipementMaj.indexOfFirst { it.nom == nomObjet }
        if (index < 0) return

        val objet = equipementMaj[index]
        val remboursement = objet.prixUnitaire

        if (objet.quantite > 1) {
            equipementMaj[index] = objet.copy(quantite = objet.quantite - 1)
        } else {
            equipementMaj.removeAt(index)
        }

        voyageurRepository.sauvegarder(
            voyageur.copy(
                equipement = equipementMaj,
                fortune    = voyageur.fortune + remboursement
            )
        )
    }
}
