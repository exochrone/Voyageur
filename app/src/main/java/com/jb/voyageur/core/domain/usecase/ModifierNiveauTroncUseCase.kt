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
        niveauCible: Int,
        confirmationAcceptee: Boolean = false
    ): Resultat {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return Resultat.Succes
        val tronc = voyageur.troncPourNom(nomTronc) ?: return Resultat.Succes

        val valeurClampee = niveauCible.coerceIn(tronc.niveauBase, 0) // commun : max 0

        // Vérification perte de niveaux individuels
        if (valeurClampee < 0 && tronc.niveauxIndividuels.isNotEmpty() && !confirmationAcceptee) {
            val membresAvecPertes = tronc.niveauxIndividuels.keys.toList()
            return Resultat.ConfirmationRequise(membresAvecPertes)
        }

        val nouvTronc = tronc.copy(
            niveauCommun = valeurClampee,
            niveauxIndividuels = if (valeurClampee < 0) emptyMap()
                                  else tronc.niveauxIndividuels
        )
        voyageurRepository.sauvegarder(voyageur.avecTronc(nouvTronc))
        return Resultat.Succes
    }
}

// Extensions helper
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
