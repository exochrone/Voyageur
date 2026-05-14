package com.jb.voyageur.core.domain.usecase

import com.google.gson.GsonBuilder
import com.jb.voyageur.core.domain.model.Voyageur
import javax.inject.Inject

class SerializerVoyageurUseCase @Inject constructor() {
    operator fun invoke(voyageur: Voyageur): String {
        // On construit une LinkedHashMap pour garantir l'ordre des sections dans le JSON
        val map = LinkedHashMap<String, Any?>()

        // 1. Description de base
        map["nom"] = voyageur.nom
        map["hautRevant"] = voyageur.hautRevant
        map["sexe"] = voyageur.sexe
        map["age"] = voyageur.age
        map["tailleCm"] = voyageur.tailleCm
        map["poidsKg"] = voyageur.poidsKg
        map["cheveux"] = voyageur.cheveux
        map["yeux"] = voyageur.yeux
        map["signeParticulier"] = voyageur.signeParticulier
        map["lateralite"] = voyageur.lateralite
        map["heureNaissance"] = voyageur.heureNaissance

        // 2. Caractéristiques
        map["caracteristiques"] = voyageur.caracteristiques
        map["beaute"] = voyageur.beaute

        // 3. Compétences
        // On peut mettre directement la map, mais l'ordre alphabétique est souvent le défaut de Gson
        // Si l'utilisateur veut un ordre précis dans les compétences aussi, il faudrait trier ici.
        map["competences"] = voyageur.competences
        map["troncCorps"] = voyageur.troncCorps
        map["troncArmes"] = voyageur.troncArmes
        map["draconic"] = voyageur.draconic

        // 4. Sorts
        if (voyageur.hautRevant) {
            map["sorts"] = voyageur.sorts
        }

        // 5. Équipement
        map["fortune"] = voyageur.fortune
        map["equipement"] = voyageur.equipement

        // 6. Archétype
        map["archetype"] = voyageur.archetype

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls() // Optionnel, pour voir les champs nulls comme age
            .create()
            
        return gson.toJson(map)
    }
}
