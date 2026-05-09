package com.jb.voyageur.feature.sorts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.SortRepository
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SortsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val sortRepository: SortRepository
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    private val _voyageur = voyageurRepository.observerVoyageur(voyageurId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<SortsUiState> = _voyageur
        .map { voyageur ->
            if (voyageur == null) SortsUiState.Loading
            else calculerUiState(voyageur)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortsUiState.Loading)

    private val _messageErreur = MutableStateFlow<String?>(null)
    val messageErreur = _messageErreur.asStateFlow()

    fun acheterSort(sort: Sort, coutTotal: Int) {
        val v = _voyageur.value ?: return
        val currentSortsPoints = v.sorts.sumOf { it.coutPaye }
        
        if (currentSortsPoints + coutTotal > 300) {
            _messageErreur.value = "Achat impossible : le quota de 300 points pour les sorts serait dépassé."
            return
        }

        viewModelScope.launch {
            val nouveauxSorts = v.sorts + SortAchete(sort.nom, sort.voie, coutTotal)
            voyageurRepository.sauvegarder(v.copy(sorts = nouveauxSorts))
        }
    }

    fun rembourserSort(sort: Sort) {
        val v = _voyageur.value ?: return
        viewModelScope.launch {
            val nouveauxSorts = v.sorts.filter { it.nom != sort.nom || it.voie != sort.voie }
            voyageurRepository.sauvegarder(v.copy(sorts = nouveauxSorts))
        }
    }

    fun effacerErreur() {
        _messageErreur.value = null
    }

    private fun calculerUiState(voyageur: Voyageur): SortsUiState.Success {
        val colonnes = mutableListOf<SortsColonne>()
        
        // 1. Sorts connus
        val sortsAchetesInfos = voyageur.sorts.map { it.nom to it.voie }.toSet()
        val tousSortsPossibles = VoieDraconic.entries.flatMap { sortRepository.chargerSorts(it) }
        val sortsAchetes = tousSortsPossibles.filter { (it.nom to it.voie) in sortsAchetesInfos }
        
        colonnes.add(SortsColonne("Sorts connus", sortsAchetes, isConnu = true))

        // 2. Voies
        VoieDraconic.entries.forEach { voie ->
            val niveau = voyageur.draconic.niveau(voie)
            if (niveau > -11) {
                val sortsVoie = sortRepository.chargerSorts(voie)
                val niveauStr = if (niveau > 0) "+$niveau" else niveau.toString()
                val titre = "${voie.name.lowercase().replaceFirstChar { it.uppercase() }} ($niveauStr)"
                colonnes.add(SortsColonne(titre, sortsVoie, voie = voie))
            }
        }

        val pointsRestants = calculPointsRestants(voyageur)

        return SortsUiState.Success(
            colonnes = colonnes,
            pointsSortsUtilises = voyageur.sorts.sumOf { it.coutPaye },
            pointsRestantsGlobal = pointsRestants,
            niveauxDraconic = VoieDraconic.entries.associateWith { voyageur.draconic.niveau(it) },
            sortsAchetes = sortsAchetesInfos,
            hautRevant = voyageur.hautRevant
        )
    }

    private fun calculPointsRestants(voyageur: Voyageur): Int {
        val pointsDraconic = voyageur.draconic.pointsTotal()
        val pointsCompetences = voyageur.competences.entries.sumOf { (nom, niveau) ->
            val famille = CatalogueCompetences.toutes
                .find { it.nom == nom }?.famille ?: return@sumOf 0
            CoutCompetence.coutCumule(famille.base, niveau)
        }
        val pointsTroncs = voyageur.troncCorps.coutTotal() + voyageur.troncArmes.coutTotal()
        
        val pointsSorts = voyageur.sorts.sumOf { it.coutPaye }

        return 3000 - pointsDraconic - pointsCompetences - pointsTroncs - pointsSorts
    }
}

sealed interface SortsUiState {
    data object Loading : SortsUiState
    data class Success(
        val colonnes: List<SortsColonne>,
        val pointsSortsUtilises: Int,
        val pointsRestantsGlobal: Int,
        val niveauxDraconic: Map<VoieDraconic, Int>,
        val sortsAchetes: Set<Pair<String, VoieDraconic>>,
        val hautRevant: Boolean
    ) : SortsUiState
}

data class SortsColonne(
    val titre: String,
    val sorts: List<Sort>,
    val isConnu: Boolean = false,
    val voie: VoieDraconic? = null
)
