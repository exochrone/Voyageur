package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Tronc
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierNiveauTroncUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    sealed interface Resultat {
        data object Succes : Resultat
        data class ConfirmationRequise(val membresAvecPertes: List<String>) : Resultat
    }

    suspend operator fun invoke(
        voyageurId: Long,
        nomTronc: String,
        membre: String,              // le membre dragué
        niveauCible: Int,
        confirmationAcceptee: Boolean = false
    ): Resultat {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return Resultat.Succes
        val tronc    = voyageur.troncPourNom(nomTronc) ?: return Resultat.Succes

        return if (tronc.estSepare) {
            // Phase séparée : modifier uniquement ce membre
            modifierMembreIndividuel(voyageur, tronc, membre, niveauCible)
        } else {
            // Phase commune : modifier le niveauCommun (tous ensemble)
            modifierNiveauCommun(voyageur, tronc, niveauCible)
        }
    }

    private suspend fun modifierNiveauCommun(
        voyageur: Voyageur,
        tronc: Tronc,
        niveauCible: Int
    ): Resultat {
        // Phase commune : max = 0, min = niveauBase
        val clampee = niveauCible.coerceIn(tronc.niveauBase, 0)
        val nouvTronc = tronc.copy(niveauCommun = clampee)
        voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
        return Resultat.Succes
    }

    private suspend fun modifierMembreIndividuel(
        voyageur: Voyageur,
        tronc: Tronc,
        membre: String,
        niveauCible: Int
    ): Resultat {
        val clampee = niveauCible.coerceIn(0, 3)

        // Si on veut descendre à 0 alors que d'autres membres sont > 0 :
        // vérifier si c'est le dernier à être > 0
        if (clampee <= 0) {
            val autresMembresSupZero = tronc.niveauxIndividuels
                .filter { it.key != membre && it.value > 0 }

            if (autresMembresSupZero.isNotEmpty()) {
                // Ce membre passe à 0, les autres restent > 0 — OK
                val nouvIndividuels = tronc.niveauxIndividuels.toMutableMap()
                nouvIndividuels.remove(membre)
                voyageurRepository.sauvegarder(
                    voyageur.avecTronc(tronc.copy(niveauxIndividuels = nouvIndividuels))
                )
                return Resultat.Succes
            }

            // Tous les membres seront à 0 (ou moins) → retour en phase commune à 0
            val nouvTronc = tronc.copy(
                niveauCommun        = 0,
                niveauxIndividuels  = emptyMap()
            )
            voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
            return Resultat.Succes
        }

        // Montée normale d'un membre
        val nouvIndividuels = tronc.niveauxIndividuels.toMutableMap()
        nouvIndividuels[membre] = clampee
        voyageurRepository.sauvegarder(
            voyageur.avecTronc(tronc.copy(niveauxIndividuels = nouvIndividuels))
        )
        return Resultat.Succes
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
