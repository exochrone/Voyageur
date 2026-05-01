package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class CreerVoyageurUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(
        nom: String,
        sexe: Sexe,
        hautRevant: Boolean
    ): Long {
        val voyageur = Voyageur(
            nom = nom,
            sexe = sexe,
            hautRevant = hautRevant
        )
        return voyageurRepository.creer(voyageur)
    }
}
