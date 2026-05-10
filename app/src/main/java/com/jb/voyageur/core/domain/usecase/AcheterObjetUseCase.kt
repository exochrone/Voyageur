package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.ObjetEquipement
import com.jb.voyageur.core.domain.model.ObjetPossede
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class AcheterObjetUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    sealed interface Resultat {
        data object Succes : Resultat
        data object FortuneInsuffisante : Resultat
    }

    suspend operator fun invoke(
        voyageurId: Long,
        objet: ObjetEquipement
    ): Resultat {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return Resultat.FortuneInsuffisante

        if (voyageur.fortune < objet.prix) {
            return Resultat.FortuneInsuffisante
        }

        val equipementMaj = voyageur.equipement.toMutableList()
        val existant = equipementMaj.indexOfFirst { it.nom == objet.nom }

        if (existant >= 0) {
            // Incrémenter la quantité
            val obj = equipementMaj[existant]
            equipementMaj[existant] = obj.copy(quantite = obj.quantite + 1)
        } else {
            // Nouvel objet
            equipementMaj.add(
                ObjetPossede(
                    nom                   = objet.nom,
                    quantite              = 1,
                    prixUnitaire          = objet.prix,
                    encombrementUnitaire  = objet.encombrement
                )
            )
        }

        voyageurRepository.sauvegarder(
            voyageur.copy(
                equipement = equipementMaj,
                fortune    = voyageur.fortune - objet.prix
            )
        )
        return Resultat.Succes
    }
}
