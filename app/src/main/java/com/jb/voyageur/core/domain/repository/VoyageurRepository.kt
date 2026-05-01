package com.jb.voyageur.core.domain.repository

import com.jb.voyageur.core.domain.model.Voyageur
import kotlinx.coroutines.flow.Flow

interface VoyageurRepository {
    suspend fun creer(voyageur: Voyageur): Long
    suspend fun sauvegarder(voyageur: Voyageur)
    fun observerVoyageur(id: Long): Flow<Voyageur?>
    suspend fun charger(id: Long): Voyageur?
}
