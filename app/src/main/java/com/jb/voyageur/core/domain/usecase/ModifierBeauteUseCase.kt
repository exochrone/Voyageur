package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierBeauteUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    sealed interface Resultat {
        data object Succes : Resultat
        data class ConfirmationRequise(val pointsPerdus: Int) : Resultat
    }

    suspend operator fun invoke(
        voyageurId: Long,
        nouvelleValeur: Int,
        confirmationAcceptee: Boolean = false
    ): Resultat {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return Resultat.Succes
        val beauteActuelle = voyageur.beaute
        val valeurClampee = nouvelleValeur.coerceIn(1, 16)

        // Détection de réduction sous 10 sans confirmation
        val franchitSeuil10 = beauteActuelle >= 10 && valeurClampee < 10
        if (franchitSeuil10 && !confirmationAcceptee) {
            val pointsPerdus = 10 - valeurClampee
            return Resultat.ConfirmationRequise(pointsPerdus)
        }

        voyageurRepository.sauvegarder(voyageur.copy(beaute = valeurClampee))
        return Resultat.Succes
    }
}
