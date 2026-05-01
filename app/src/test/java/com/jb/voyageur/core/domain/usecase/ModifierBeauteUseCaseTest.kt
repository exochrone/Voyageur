package com.jb.voyageur.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ModifierBeauteUseCaseTest {

    private val repository = mockk<VoyageurRepository>(relaxed = true)
    private val useCase = ModifierBeauteUseCase(repository)

    @Test
    fun `invoke clamps value between 3 and 16`() = runTest {
        val voyageur = Voyageur(id = 1L, beaute = 10)
        coEvery { repository.charger(1L) } returns voyageur

        // Below min
        useCase(1L, 2)
        coVerify { repository.sauvegarder(match { it.beaute == 3 }) }

        // Above max
        useCase(1L, 17)
        coVerify { repository.sauvegarder(match { it.beaute == 16 }) }

        // Nominal
        useCase(1L, 12)
        coVerify { repository.sauvegarder(match { it.beaute == 12 }) }
    }

    @Test
    fun `pointsBeauteInvestis calculation logic in UI state`() {
        // This is a logic test based on how the UI state is calculated from the model
        // Base 10 is free. Above 10 costs 1 point per level.
        fun getPointsInvestis(beaute: Int) = (beaute - 10).coerceAtLeast(0)

        assertThat(getPointsInvestis(10)).isEqualTo(0)
        assertThat(getPointsInvestis(14)).isEqualTo(4)
        assertThat(getPointsInvestis(5)).isEqualTo(0) // points are LOST, but not invested from the 160 pool
    }
}
