package com.jb.voyageur.feature.competences

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetencesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val modifierCompetenceUseCase: ModifierCompetenceUseCase,
    private val modifierNiveauTroncUseCase: ModifierNiveauTroncUseCase,
    private val modifierMembreTroncUseCase: ModifierMembreTroncUseCase,
    private val modifierSurvieSpecifiqueUseCase: ModifierSurvieSpecifiqueUseCase,
    private val modifierDraconicUseCase: ModifierDraconicUseCase
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    val uiState: StateFlow<CompetencesUiState> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { voyageur -> voyageur.toCompetencesUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CompetencesUiState.Loading
        )

    private val _aideActive = MutableStateFlow<String?>(null)
    val aideActive: StateFlow<String?> = _aideActive.asStateFlow()

    fun onCompetenceChange(nom: String, base: Int, niveau: Int) {
        viewModelScope.launch {
            if (nom in CatalogueCompetences.SURVIES_SPECIFIQUES) {
                modifierSurvieSpecifiqueUseCase(voyageurId, nom, niveau)
            } else {
                modifierCompetenceUseCase(voyageurId, nom, base, niveau)
            }
        }
    }

    fun onNiveauCommunChange(nomTronc: String, niveau: Int) {
        viewModelScope.launch {
            modifierNiveauTroncUseCase(voyageurId, nomTronc, niveau)
        }
    }

    fun onMembreTroncChange(nomTronc: String, membre: String, niveau: Int) {
        viewModelScope.launch {
            modifierMembreTroncUseCase(voyageurId, nomTronc, membre, niveau)
        }
    }

    fun onDraconicChange(voie: VoieDraconic, niveau: Int) {
        viewModelScope.launch {
            modifierDraconicUseCase(voyageurId, voie, niveau)
        }
    }

    fun onDemanderAide(nom: String) {
        _aideActive.value = nom
    }

    fun onFermerAide() {
        _aideActive.value = null
    }
}

sealed interface CompetencesUiState {
    data object Loading : CompetencesUiState
    data class Success(
        val colonnes: List<ColonneCompetences>,
        val pointsRestants: Int,
        val hautRevant: Boolean
    ) : CompetencesUiState
}

data class ColonneCompetences(
    val famille: FamilleCompetence,
    val items: List<CompetenceUiItem>
)

sealed interface CompetenceUiItem {
    data class Individuelle(
        val competence: Competence,
        val niveauActuel: Int,
        val coutCumule: Int,
        val estBloquee: Boolean = false
    ) : CompetenceUiItem

    data class TroncCommun(
        val tronc: Tronc,
        val label: String
    ) : CompetenceUiItem
}

private fun Voyageur.toCompetencesUiState(): CompetencesUiState.Success {
    val pointsDraconic = draconic.pointsTotal()
    val pointsCompetences = competences.entries.sumOf { (nom, niveau) ->
        val famille = CatalogueCompetences.toutes
            .find { it.nom == nom }?.famille ?: return@sumOf 0
        CoutCompetence.coutCumule(famille.base, niveau)
    }
    val pointsTroncs = troncCorps.coutTotal() + troncArmes.coutTotal()
    // pointsSorts not yet implemented in model
    val pointsRestants = 3000 - pointsDraconic - pointsCompetences - pointsTroncs

    val colonnes = mutableListOf<ColonneCompetences>()
    
    FamilleCompetence.entries.forEach { famille ->
        if (famille == FamilleCompetence.DRACONIC && !hautRevant) return@forEach
        
        val items = mutableListOf<CompetenceUiItem>()
        
        if (famille == FamilleCompetence.COMBAT_MELEE) {
            // Troncs
            items.add(CompetenceUiItem.TroncCommun(troncCorps, "Tronc Corps à corps"))
            items.add(CompetenceUiItem.TroncCommun(troncArmes, "Tronc Armes"))
            // Indépendantes
            CatalogueCompetences.parFamille[famille]
                ?.filter { it.appartientAuTronc == null }
                ?.forEach { comp ->
                    items.add(construireItemIndividuel(comp, this))
                }
        } else if (famille == FamilleCompetence.DRACONIC) {
            VoieDraconic.entries.forEach { voie ->
                val niveau = draconic.niveau(voie)
                val multiplicateur = draconic.multiplicateurPour(voie)
                items.add(CompetenceUiItem.Individuelle(
                    competence = Competence(voie.name.lowercase().replaceFirstChar { it.uppercase() }, FamilleCompetence.DRACONIC),
                    niveauActuel = niveau,
                    coutCumule = CoutCompetence.coutCumuleAvecMultiplicateur(-11, niveau, multiplicateur)
                ))
            }
        } else {
            CatalogueCompetences.parFamille[famille]?.forEach { comp ->
                items.add(construireItemIndividuel(comp, this))
            }
        }
        
        colonnes.add(ColonneCompetences(famille, items))
    }

    return CompetencesUiState.Success(colonnes, pointsRestants, hautRevant)
}

private fun construireItemIndividuel(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
    val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
    val survieExterieur = voyageur.competences["Survie en extérieur"] ?: -8
    
    val estBloquee = competence.nom in CatalogueCompetences.SURVIES_SPECIFIQUES && survieExterieur < 0
    
    return CompetenceUiItem.Individuelle(
        competence = competence,
        niveauActuel = niveauActuel,
        coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
        estBloquee = estBloquee
    )
}
