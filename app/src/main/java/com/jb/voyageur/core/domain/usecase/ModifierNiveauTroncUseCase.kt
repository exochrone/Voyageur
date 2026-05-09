package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Tronc
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierNiveauTroncUseCase @Inject constructor(
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

        if (tronc.estSepare) {
            // Phase séparée (>= 0)
            if (niveauCible < 0) {
                // Tentative de passage sous zéro
                val autresMembresSupZero = tronc.niveauxIndividuels.filter { it.key != membre && it.value > 0 }
                if (autresMembresSupZero.isEmpty()) {
                    // OK pour descendre : tout le tronc passe à niveauCible
                    val clampee = niveauCible.coerceIn(tronc.niveauBase, -1)
                    val nouvTronc = tronc.copy(
                        niveauCommun = clampee,
                        niveauxIndividuels = emptyMap(),
                        membreAncreCommun = membre // Devient l'ancre en repassant sous zéro
                    )
                    voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
                }
            } else {
                // Modification individuelle au dessus de 0
                val clampee = niveauCible.coerceIn(0, 3)
                val nouvIndividuels = tronc.niveauxIndividuels.toMutableMap()
                if (clampee == 0) {
                    nouvIndividuels.remove(membre)
                } else {
                    nouvIndividuels[membre] = clampee
                }
                voyageurRepository.sauvegarder(voyageur.avecTronc(tronc.copy(niveauxIndividuels = nouvIndividuels)))
            }
        } else {
            // Phase commune (< 0)
            if (niveauCible >= 0) {
                // Passage à la phase séparée
                val clampee = niveauCible.coerceIn(0, 3)
                val nouvIndividuels = if (clampee > 0) mapOf(membre to clampee) else emptyMap()
                val nouvTronc = tronc.copy(
                    niveauCommun = 0,
                    niveauxIndividuels = nouvIndividuels,
                    membreAncreCommun = membre // Reste l'ancre car c'est lui qui a fait passer à 0
                )
                voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
            } else {
                // Reste en phase commune
                val clampee = niveauCible.coerceIn(tronc.niveauBase, -1)
                val nouvTronc = tronc.copy(
                    niveauCommun = clampee,
                    membreAncreCommun = membre // L'ancre suit le drag en phase commune
                )
                voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
            }
        }
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
