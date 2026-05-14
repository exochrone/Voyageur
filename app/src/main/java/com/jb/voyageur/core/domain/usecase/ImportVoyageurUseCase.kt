package com.jb.voyageur.core.domain.usecase

import com.google.gson.Gson
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import javax.inject.Inject

class ImportVoyageurUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository
) {
    suspend operator fun invoke(json: String): Long {
        val gson = Gson()
        val voyageur = gson.fromJson(json, Voyageur::class.java)
        // Ensure ID is reset to 0 to trigger a new ID generation in the repository
        // OR if the user wants to OVERWRITE, but here the request implies creating/loading it.
        // Usually, importing a file should create a new entry in our local database.
        val voyageurToSave = voyageur.copy(id = 0)
        return voyageurRepository.creer(voyageurToSave)
    }
}
