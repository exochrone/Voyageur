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

    fun onCompetenceTappee(nomCompetence: String) {
        val niveauCourant = _niveauSelectionne.value
        viewModelScope.launch {
            modifierArchetypeUseCase(voyageurId, nomCompetence, niveauCourant)

            // Après attribution : si le niveau est épuisé, avancer automatiquement
            // 1. Chercher le plus haut niveau inférieur disponible
            // 2. Sinon chercher le plus bas niveau supérieur disponible
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val restants = calcQuantitesRestantes(voyageur.archetype)
            if ((restants[niveauCourant] ?: 0) <= 0) {
                val plusBas = restants.entries
                    .filter { it.key < niveauCourant && it.value > 0 }
                    .maxByOrNull { it.key }?.key
                val plusHaut = restants.entries
                    .filter { it.key > niveauCourant && it.value > 0 }
                    .minByOrNull { it.key }?.key
                val prochain = plusBas ?: plusHaut
                if (prochain != null) _niveauSelectionne.value = prochain
            }
        }
    }

    private fun Voyageur.toArchetypeUiState(niveauSelectionne: Int): ArchetypeUiState.Success {
        val quantitesRestantes = calcQuantitesRestantes(archetype)
        val estComplet         = archetype.estComplet()

        val colonneGauche = Archetype.NIVEAUX_DISPONIBLES
            .filterKeys { it > 0 }  // On n'affiche pas "0" dans la colonne gauche
            .map { (niveau, total) ->
                NiveauItem(
                    niveau      = niveau,
                    restant     = quantitesRestantes[niveau] ?: 0,
                    selectionne = niveau == niveauSelectionne
                )
            }.sortedByDescending { it.niveau }

        val colonneDroite = CATEGORIES_ARCHETYPE.map { categorie ->
            CategorieArchetype(
                titre = categorie.titreRes,
                competences = categorie.competences.map { nom ->
                    val niveauAttribue = archetype.niveaux[nom]
                    val affichage = when {
                        niveauAttribue != null -> niveauAttribue
                        estComplet             -> 0     // tous attribués → reste = 0
                        else                   -> null  // non attribué → "-"
                    }
                    CompetenceArchetype(
                        nom     = nom,
                        niveau  = affichage
                    )
                }
            )
        }

        return ArchetypeUiState.Success(
            colonneGauche      = colonneGauche,
            colonneDroite      = colonneDroite,
            niveauSelectionne  = niveauSelectionne,
            estComplet         = estComplet,
            hautRevant         = hautRevant
        )
    }
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
        val colonneGauche:     List<NiveauItem>,
        val colonneDroite:     List<CategorieArchetype>,
        val niveauSelectionne: Int,
        val estComplet:        Boolean,
        val hautRevant:        Boolean
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
    val nom:    String,
    val niveau: Int?     // null = "-", 0..11 = attribué
)
