package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class CreerVoyageurUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository,
    private val mettreAJourPhysiqueUseCase: MettreAJourPhysiqueUseCase
) {
    suspend operator fun invoke(
        nom: String,
        sexe: Sexe,
        hautRevant: Boolean
    ): Long {
        val voyageur = Voyageur(
            nom = nom,
            sexe = sexe,
            hautRevant = hautRevant,
            age = 20 // Default age remains 20 as per business logic, but weight/height should be generated
        )
        val id = voyageurRepository.creer(voyageur)
        mettreAJourPhysiqueUseCase.generer(id)
        return id
    }
}
