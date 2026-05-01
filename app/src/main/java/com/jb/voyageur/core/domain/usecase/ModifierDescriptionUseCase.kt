package com.jb.voyageur.core.domain.usecase

import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ModifierDescriptionUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(voyageurId: Long, champ: ChampDescription, valeur: String) {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return
        val updated = when (champ) {
            ChampDescription.NOM -> voyageur.copy(nom = valeur)
            ChampDescription.CHEVEUX -> voyageur.copy(cheveux = valeur)
            ChampDescription.YEUX -> voyageur.copy(yeux = valeur)
            ChampDescription.SIGNE_PARTICULIER -> voyageur.copy(signeParticulier = valeur)
            ChampDescription.AGE -> voyageur.copy(age = valeur.toIntOrNull())
            ChampDescription.TAILLE_CM -> voyageur.copy(tailleCm = valeur.toIntOrNull())
            ChampDescription.POIDS_KG -> voyageur.copy(poidsKg = valeur.toIntOrNull())
            ChampDescription.SEXE -> voyageur.copy(sexe = Sexe.valueOf(valeur))
        }
        voyageurRepository.sauvegarder(updated)
    }
}

enum class ChampDescription {
    NOM, CHEVEUX, YEUX, SIGNE_PARTICULIER, AGE, TAILLE_CM, POIDS_KG, SEXE
}
