package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Tronc
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierMembreTroncUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        nomTronc: String,
        membre: String,
        niveauCible: Int
    ) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val tronc = voyageur.troncPourNom(nomTronc) ?: return

        // Membres individuels : de 0 à +3 uniquement
        val valeurClampee = niveauCible.coerceIn(0, 3)
        val nouvIndividuels = tronc.niveauxIndividuels.toMutableMap()
        if (valeurClampee == 0) {
            nouvIndividuels.remove(membre)
        } else {
            nouvIndividuels[membre] = valeurClampee
        }
        val nouvTronc = tronc.copy(niveauxIndividuels = nouvIndividuels)
        voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
    }
}

private fun Voyageur.troncPourNom(nom: String): Tronc? = when (nom) {
    "TroncCorps" -> troncCorps
    "TroncArmes" -> troncArmes
    else -> null
}

private fun Voyageur.avecTronc(tronc: Tronc): Voyageur = when (tronc.nom) {
    "TroncCorps" -> copy(troncCorps = tronc)
    "TroncArmes" -> copy(troncArmes = tronc)
    else -> this
}
