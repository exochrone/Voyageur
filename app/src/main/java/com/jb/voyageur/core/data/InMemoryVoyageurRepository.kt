package com.jb.voyageur.core.data

import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryVoyageurRepository @Inject constructor() : VoyageurRepository {
    private val voyageurs = MutableStateFlow<Map<Long, Voyageur>>(emptyMap())
    private var nextId = 1L

    override suspend fun creer(voyageur: Voyageur): Long {
        val id = nextId++
        val nouveauVoyageur = voyageur.copy(id = id)
        voyageurs.value += (id to nouveauVoyageur)
        return id
    }

    override suspend fun sauvegarder(voyageur: Voyageur) {
        voyageurs.value += (voyageur.id to voyageur)
    }

    override fun observerVoyageur(id: Long): Flow<Voyageur?> {
        return voyageurs.map { it[id] }
    }

    override suspend fun charger(id: Long): Voyageur? {
        return voyageurs.value[id]
    }
}
