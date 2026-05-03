package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.TableFourchettePhysique
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class MettreAJourPhysiqueUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    /**
     * Appelé quand TAILLE ou Sexe change.
     * Génère de nouvelles valeurs aléatoires dans les nouvelles fourchettes.
     */
    suspend fun generer(voyageurId: Long) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val taille = voyageur.caracteristiques.taille
        val sexe   = voyageur.sexe

        val nouvPoids    = TableFourchettePhysique.poidsAleatoire(taille)
        val nouvTailleCm = TableFourchettePhysique.tailleCmAleatoire(taille, sexe)

        voyageurRepository.sauvegarder(
            voyageur.copy(
                poidsKg  = nouvPoids,
                tailleCm = nouvTailleCm
            )
        )
    }

    /**
     * Appelé quand l'utilisateur saisit manuellement une valeur.
     * Clampe à la fourchette correspondante sans régénérer l'autre valeur.
     */
    suspend fun clamperPoids(voyageurId: Long, valeurSaisie: Int) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val clampee  = TableFourchettePhysique.clamperPoids(
            taille = voyageur.caracteristiques.taille,
            valeur = valeurSaisie
        )
        voyageurRepository.sauvegarder(voyageur.copy(poidsKg = clampee))
    }

    suspend fun clamperTailleCm(voyageurId: Long, valeurSaisie: Int) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val clampee  = TableFourchettePhysique.clamperTailleCm(
            taille = voyageur.caracteristiques.taille,
            sexe   = voyageur.sexe,
            valeur = valeurSaisie
        )
        voyageurRepository.sauvegarder(voyageur.copy(tailleCm = clampee))
    }
}
