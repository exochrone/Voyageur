package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Archetype
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierArchetypeUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    /**
     * Attribue ou retire le niveau sélectionné d'une compétence.
     *
     * - Si la compétence n'a pas de niveau → attribue [niveauSelectionne]
     * - Si la compétence a déjà [niveauSelectionne] → retire le niveau (retour à null)
     * - Si la compétence a un niveau différent → remplace par [niveauSelectionne]
     */
    suspend operator fun invoke(
        voyageurId: Long,
        nomCompetence: String,
        niveauSelectionne: Int
    ) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val niveauxMaj = voyageur.archetype.niveaux.toMutableMap()

        if (niveauxMaj[nomCompetence] == niveauSelectionne) {
            // Retrait : retour à "-"
            niveauxMaj.remove(nomCompetence)
        } else {
            // Attribution ou remplacement
            niveauxMaj[nomCompetence] = niveauSelectionne
        }

        voyageurRepository.sauvegarder(
            voyageur.copy(archetype = Archetype(niveauxMaj))
        )
    }
}
