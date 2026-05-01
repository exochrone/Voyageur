package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierCaracteristiqueUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        voyageurId: Long,
        champ: ChampCaracteristique,
        nouvelleValeur: Int
    ) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val caracs = voyageur.caracteristiques

        val valeurClampee = nouvelleValeur.coerceIn(6, 15)
        val nouvCaracs = when (champ) {
            ChampCaracteristique.TAILLE -> {
                val nouvTaille = valeurClampee
                val forceMax = nouvTaille + 4
                caracs.copy(
                    taille = nouvTaille,
                    force = caracs.force.coerceAtMost(forceMax)
                )
            }
            ChampCaracteristique.FORCE ->
                caracs.copy(force = valeurClampee.coerceAtMost(caracs.taille + 4))
            ChampCaracteristique.APPARENCE -> caracs.copy(apparence = valeurClampee)
            ChampCaracteristique.CONSTITUTION -> caracs.copy(constitution = valeurClampee)
            ChampCaracteristique.AGILITE -> caracs.copy(agilite = valeurClampee)
            ChampCaracteristique.DEXTERITE -> caracs.copy(dexterite = valeurClampee)
            ChampCaracteristique.VUE -> caracs.copy(vue = valeurClampee)
            ChampCaracteristique.OUIE -> caracs.copy(ouie = valeurClampee)
            ChampCaracteristique.ODO_GOUT -> caracs.copy(odoGout = valeurClampee)
            ChampCaracteristique.VOLONTE -> caracs.copy(volonte = valeurClampee)
            ChampCaracteristique.INTELLECT -> caracs.copy(intellect = valeurClampee)
            ChampCaracteristique.EMPATHIE -> caracs.copy(empathie = valeurClampee)
            ChampCaracteristique.REVE -> caracs.copy(reve = valeurClampee)
            ChampCaracteristique.CHANCE -> caracs.copy(chance = valeurClampee)
        }
        voyageurRepository.sauvegarder(voyageur.copy(caracteristiques = nouvCaracs))
    }
}

enum class ChampCaracteristique {
    TAILLE, APPARENCE, CONSTITUTION, FORCE, AGILITE, DEXTERITE,
    VUE, OUIE, ODO_GOUT, VOLONTE, INTELLECT, EMPATHIE, REVE, CHANCE
}
