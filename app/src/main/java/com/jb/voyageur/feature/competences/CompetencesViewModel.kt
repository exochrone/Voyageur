package com.jb.voyageur.feature.competences

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CompetencesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository
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

    fun onDemanderAide(nom: String) {
        _aideActive.value = nom
    }

    fun onFermerAide() {
        _aideActive.value = null
    }

    private fun Voyageur.toCompetencesUiState(): CompetencesUiState.Success {
        val pointsDraconic = draconic.pointsTotal()
        val pointsCompetences = competences.entries.sumOf { (nom, niveau) ->
            val famille = CatalogueCompetences.toutes
                .find { it.nom == nom }?.famille ?: return@sumOf 0
            CoutCompetence.coutCumule(famille.base, niveau)
        }
        val pointsTroncs = troncCorps.coutTotal() + troncArmes.coutTotal()
        val pointsRestants = 3000 - pointsDraconic - pointsCompetences - pointsTroncs

        val colonnes = buildColonnes(this)
        return CompetencesUiState.Success(colonnes, pointsRestants, hautRevant)
    }

    private fun buildColonnes(voyageur: Voyageur): List<ColonneCompetences> {
        val result = mutableListOf<ColonneCompetences>()

        // 1. Générales
        result.add(ColonneCompetences(
            famille = FamilleCompetence.GENERALE,
            items = buildItems(FamilleCompetence.GENERALE, voyageur)
        ))

        // 2. Particulières
        result.add(ColonneCompetences(
            famille = FamilleCompetence.PARTICULIERE,
            items = buildItems(FamilleCompetence.PARTICULIERE, voyageur)
        ))

        // 3. Combat mêlée
        result.add(ColonneCompetences(
            famille = FamilleCompetence.COMBAT_MELEE,
            items = buildItemsCombat(voyageur)
        ))

        // 4. Tir et Lancer
        result.add(ColonneCompetences(
            famille = FamilleCompetence.TIR_LANCER,
            items = buildItems(FamilleCompetence.TIR_LANCER, voyageur)
        ))

        // 5. Connaissances
        result.add(ColonneCompetences(
            famille = FamilleCompetence.CONNAISSANCE,
            items = buildItems(FamilleCompetence.CONNAISSANCE, voyageur)
        ))

        // 6. Spécialisées
        result.add(ColonneCompetences(
            famille = FamilleCompetence.SPECIALISEE,
            items = buildItems(FamilleCompetence.SPECIALISEE, voyageur)
        ))

        // 7. Draconic
        if (voyageur.hautRevant) {
            result.add(ColonneCompetences(
                famille = FamilleCompetence.DRACONIC,
                items = buildItemsDraconic(voyageur)
            ))
        }

        return result
    }

    private fun buildItems(famille: FamilleCompetence, voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[famille]
            ?.map { comp ->
                val niveauActuel = voyageur.competences[comp.nom] ?: comp.niveauBase
                CompetenceUiItem.Individuelle(
                    competence = comp,
                    niveauActuel = niveauActuel,
                    coutCumule = CoutCompetence.coutCumule(comp.niveauBase, niveauActuel)
                )
            } ?: emptyList()

    private fun buildItemsCombat(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> {
        val items = mutableListOf<CompetenceUiItem.Individuelle>()
        voyageur.troncCorps.membres.forEach { nom ->
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncCorps")
            items.add(CompetenceUiItem.Individuelle(comp, voyageur.troncCorps.niveauPour(nom), 0))
        }
        voyageur.troncArmes.membres.forEach { nom ->
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncArmes")
            items.add(CompetenceUiItem.Individuelle(comp, voyageur.troncArmes.niveauPour(nom), 0))
        }
        CatalogueCompetences.parFamille[FamilleCompetence.COMBAT_MELEE]
            ?.filter { it.appartientAuTronc == null }
            ?.forEach { comp ->
                val niveauActuel = voyageur.competences[comp.nom] ?: comp.niveauBase
                items.add(CompetenceUiItem.Individuelle(comp, niveauActuel, CoutCompetence.coutCumule(comp.niveauBase, niveauActuel)))
            }
        return items
    }

    private fun buildItemsDraconic(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        VoieDraconic.entries.map { voie ->
            val niveau = voyageur.draconic.niveau(voie)
            val multiplicateur = voyageur.draconic.multiplicateurPour(voie)
            val comp = Competence(
                nom = voie.name.lowercase().replaceFirstChar { it.uppercase() },
                famille = FamilleCompetence.DRACONIC
            )
            CompetenceUiItem.Individuelle(comp, niveau, CoutCompetence.coutCumuleAvecMultiplicateur(-11, niveau, multiplicateur))
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
    val items: List<CompetenceUiItem.Individuelle>
)

sealed interface CompetenceUiItem {
    data class Individuelle(
        val competence: Competence,
        val niveauActuel: Int,
        val coutCumule: Int
    ) : CompetenceUiItem
}
