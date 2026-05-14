package com.jb.voyageur.feature.archetype

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.Archetype
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.ModifierArchetypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchetypeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val modifierArchetypeUseCase: ModifierArchetypeUseCase
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    // Niveau sélectionné dans la colonne gauche — initialisé à 11
    private val _niveauSelectionne = MutableStateFlow(11)
    val niveauSelectionne: StateFlow<Int> = _niveauSelectionne.asStateFlow()

    val uiState: StateFlow<ArchetypeUiState> = combine(
        voyageurRepository.observerVoyageur(voyageurId).filterNotNull(),
        _niveauSelectionne
    ) { voyageur, niveauSelectionne ->
        voyageur.toArchetypeUiState(niveauSelectionne)
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArchetypeUiState.Loading
    )

    fun onNiveauSelectionne(niveau: Int) {
        _niveauSelectionne.value = niveau
    }

    fun onCompetenceTappee(keyCompetence: String) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val niveauExistant = voyageur.archetype.niveaux[keyCompetence]

            if (niveauExistant != null) {
                // Reprendre le niveau existant (1 à 11)
                modifierArchetypeUseCase(voyageurId, keyCompetence, niveauExistant)
                // Le ▶ se place à côté du niveau repris
                _niveauSelectionne.value = niveauExistant
            } else if (!voyageur.archetype.estComplet()) {
                // Attribution (uniquement si non complet)
                val niveauAAttribuer = _niveauSelectionne.value
                modifierArchetypeUseCase(voyageurId, keyCompetence, niveauAAttribuer)

                // Avancement automatique si le niveau est épuisé
                val voyageurMaj = voyageurRepository.charger(voyageurId) ?: return@launch
                val restants = calcQuantitesRestantes(voyageurMaj.archetype)
                if ((restants[niveauAAttribuer] ?: 0) <= 0) {
                    val plusBas = restants.entries
                        .filter { it.key < niveauAAttribuer && it.value > 0 }
                        .maxByOrNull { it.key }?.key
                    val plusHaut = restants.entries
                        .filter { it.key > niveauAAttribuer && it.value > 0 }
                        .minByOrNull { it.key }?.key
                    val prochain = plusBas ?: plusHaut
                    if (prochain != null) _niveauSelectionne.value = prochain
                }
            }
        }
    }

    private fun Voyageur.toArchetypeUiState(niveauSelectionne: Int): ArchetypeUiState.Success {
        val quantitesRestantes = calcQuantitesRestantes(archetype)
        val estComplet         = archetype.estComplet()

        // Extract custom skills and group them by family
        val customSkillsByFamily = competences.keys
            .asSequence()
            .filter { it.startsWith("CUSTOM:") }
            .map { key ->
                val parts = key.split(":")
                val familleName = parts[1]
                val nom = parts.last()
                familleName to (key to nom)
            }
            .groupBy({ it.first }, { it.second })

        val colonneGauche = Archetype.NIVEAUX_DISPONIBLES
            .filterKeys { it > 0 }
            .map { (niveau, _) ->
                NiveauItem(
                    niveau      = niveau,
                    restant     = quantitesRestantes[niveau] ?: 0,
                    // Si complet, plus de sélection
                    selectionne = !estComplet && niveau == niveauSelectionne
                )
            }.sortedByDescending { it.niveau }

        val colonneDroite = CATEGORIES_ARCHETYPE.map { categorie ->
            val baseSkills = categorie.competences.map { it to it } // (key, display)
            val customSkills = customSkillsByFamily[categorie.famille.name] ?: emptyList()
            
            val allSkillsForThisCategory = baseSkills + customSkills

            CategorieArchetype(
                titre = categorie.titreRes,
                competences = allSkillsForThisCategory.map { (key, nom) ->
                    val niveauAttribue = archetype.niveaux[key]
                    val affichage = when {
                        niveauAttribue != null -> niveauAttribue
                        estComplet             -> 0     // tous attribués → reste = 0
                        else                   -> null  // non attribué → "-"
                    }
                    
                    val niveauActuel = getNiveauActuel(this, key)
                    val estGrise = !estComplet && affichage == null && niveauActuel > niveauSelectionne

                    CompetenceArchetype(
                        nom     = nom,
                        key     = key,
                        niveau  = affichage,
                        estGrise = estGrise
                    )
                }
            )
        }

        val aDesSortsAccessibles = hautRevant && (
            draconic.oniros > -11 ||
            draconic.hypnos > -11 ||
            draconic.narcos > -11 ||
            draconic.thanatos > -11
        )

        return ArchetypeUiState.Success(
            colonneGauche        = colonneGauche,
            colonneDroite        = colonneDroite,
            niveauSelectionne    = niveauSelectionne,
            estComplet           = estComplet,
            aDesSortsAccessibles = aDesSortsAccessibles
        )
    }
}

private fun getNiveauActuel(voyageur: Voyageur, key: String): Int {
    // 1. Draconic
    val voie = try { com.jb.voyageur.core.domain.model.VoieDraconic.valueOf(key.uppercase()) } catch (e: Exception) { null }
    if (voie != null) return voyageur.draconic.niveau(voie)

    // 2. Troncs
    if (voyageur.troncCorps.membres.contains(key)) return voyageur.troncCorps.niveauPour(key)
    if (voyageur.troncArmes.membres.contains(key)) return voyageur.troncArmes.niveauPour(key)

    // 3. Compétences individuelles (standards et customs)
    val base = if (key.startsWith("CUSTOM:")) {
        try { 
            val familleName = key.split(":")[1]
            com.jb.voyageur.core.domain.model.FamilleCompetence.valueOf(familleName).base
        } catch (e: Exception) { -4 }
    } else {
        com.jb.voyageur.core.domain.model.CatalogueCompetences.toutes.find { it.nom == key }?.famille?.base ?: -4
    }

    return voyageur.competences[key] ?: base
}

/** Calcule pour chaque niveau le nombre d'attributions restantes. */
private fun calcQuantitesRestantes(archetype: Archetype): Map<Int, Int> {
    val utilises = archetype.niveaux.values
        .groupingBy { it }
        .eachCount()

    return Archetype.NIVEAUX_DISPONIBLES
        .filterKeys { it > 0 }
        .mapValues { (niveau, total) ->
            (total - (utilises[niveau] ?: 0)).coerceAtLeast(0)
        }
}

sealed interface ArchetypeUiState {
    data object Loading : ArchetypeUiState
    data class Success(
        val colonneGauche:        List<NiveauItem>,
        val colonneDroite:        List<CategorieArchetype>,
        val niveauSelectionne:    Int,
        val estComplet:           Boolean,
        val aDesSortsAccessibles: Boolean
    ) : ArchetypeUiState
}

data class NiveauItem(
    val niveau:      Int,
    val restant:     Int,     // combien peuvent encore être attribués
    val selectionne: Boolean
)

data class CategorieArchetype(
    val titre:        Int,               // stringRes
    val competences:  List<CompetenceArchetype>
)

data class CompetenceArchetype(
    val nom:      String,
    val key:      String,
    val niveau:   Int?,     // null = "-", 0..11 = attribué
    val estGrise: Boolean = false
)
