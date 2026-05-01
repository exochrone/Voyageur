package com.jb.voyageur.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.jb.voyageur.core.domain.model.Caracteristiques
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ModifierCaracteristiqueUseCaseTest {

    private val repository = mockk<VoyageurRepository>(relaxed = true)
    private val useCase = ModifierCaracteristiqueUseCase(repository)

    @Test
    fun `force is capped by taille plus 4`() = runTest {
        val voyageur = Voyageur(id = 1L, caracteristiques = Caracteristiques(taille = 8, force = 8))
        coEvery { repository.charger(1L) } returns voyageur

        // Try to set force to 15 (cap is 8 + 4 = 12)
        useCase(1L, ChampCaracteristique.FORCE, 15)
        coVerify { repository.sauvegarder(match { it.caracteristiques.force == 12 }) }
    }

    @Test
    fun `reducing taille automatically reduces force if needed`() = runTest {
        // Force 13 is valid for Taille 10 (max 14)
        val voyageur = Voyageur(id = 1L, caracteristiques = Caracteristiques(taille = 10, force = 13))
        coEvery { repository.charger(1L) } returns voyageur

        // Reduce Taille to 7 (new force max is 7 + 4 = 11)
        useCase(1L, ChampCaracteristique.TAILLE, 7)
        coVerify { repository.sauvegarder(match { 
            it.caracteristiques.taille == 7 && it.caracteristiques.force == 11 
        }) }
    }
}
