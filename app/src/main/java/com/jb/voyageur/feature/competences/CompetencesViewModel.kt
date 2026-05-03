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

        val doubleListes = buildDoubleListes(this)
        return CompetencesUiState.Success(doubleListes, pointsRestants, hautRevant)
    }

    private fun buildDoubleListes(voyageur: Voyageur): List<DoubleListe> {
        val result = mutableListOf<DoubleListe>()

        // Double-liste 1 : Générales | Particulières
        result.add(DoubleListe(
            gauche = ListeCompetences(
                famille = FamilleCompetence.GENERALE,
                items = buildItemsGenerales(voyageur)
            ),
            droite = ListeCompetences(
                famille = FamilleCompetence.PARTICULIERE,
                items = buildItemsParticulieres(voyageur)
            )
        ))

        // Double-liste 2 : Combat mêlée | Tir et Lancer
        result.add(DoubleListe(
            gauche = ListeCompetences(
                famille = FamilleCompetence.COMBAT_MELEE,
                items = buildItemsCombat(voyageur)
            ),
            droite = ListeCompetences(
                famille = FamilleCompetence.TIR_LANCER,
                items = buildItemsTirLancer(voyageur)
            )
        ))

        // Double-liste 3 : Connaissances + Draconic (gauche) | Spécialisées (droite)
        val itemsGauche = buildItemsConnaissances(voyageur).toMutableList<CompetenceUiItem>()
        if (voyageur.hautRevant) {
            itemsGauche.add(CompetenceUiItem.Separateur(FamilleCompetence.DRACONIC.labelRes))
            itemsGauche.addAll(buildItemsDraconic(voyageur))
        }

        result.add(DoubleListe(
            gauche = ListeCompetences(
                famille = FamilleCompetence.CONNAISSANCE,
                items = itemsGauche
            ),
            droite = ListeCompetences(
                famille = FamilleCompetence.SPECIALISEE,
                items = buildItemsSpecialisees(voyageur)
            )
        ))

        return result
    }

    private fun buildItemsGenerales(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[FamilleCompetence.GENERALE]
            ?.map { comp -> construireItemIndividuel(comp, voyageur) }
            ?: emptyList()

    private fun buildItemsParticulieres(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[FamilleCompetence.PARTICULIERE]
            ?.map { comp -> construireItemIndividuel(comp, voyageur) }
            ?: emptyList()

    private fun buildItemsSpecialisees(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[FamilleCompetence.SPECIALISEE]
            ?.map { comp -> construireItemIndividuel(comp, voyageur) }
            ?: emptyList()

    private fun buildItemsConnaissances(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[FamilleCompetence.CONNAISSANCE]
            ?.map { comp -> construireItemIndividuel(comp, voyageur) }
            ?: emptyList()

    private fun buildItemsTirLancer(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        CatalogueCompetences.parFamille[FamilleCompetence.TIR_LANCER]
            ?.map { comp -> construireItemIndividuel(comp, voyageur) }
            ?: emptyList()

    private fun buildItemsCombat(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> {
        val items = mutableListOf<CompetenceUiItem.Individuelle>()
        voyageur.troncCorps.membres.forEach { membreNom ->
            val niveau = voyageur.troncCorps.niveauPour(membreNom)
            val comp = CatalogueCompetences.toutes.find { it.nom == membreNom }
                ?: Competence(membreNom, FamilleCompetence.COMBAT_MELEE, "TroncCorps")
            items.add(CompetenceUiItem.Individuelle(comp, niveau, 0))
        }
        voyageur.troncArmes.membres.forEach { membreNom ->
            val niveau = voyageur.troncArmes.niveauPour(membreNom)
            val comp = CatalogueCompetences.toutes.find { it.nom == membreNom }
                ?: Competence(membreNom, FamilleCompetence.COMBAT_MELEE, "TroncArmes")
            items.add(CompetenceUiItem.Individuelle(comp, niveau, 0))
        }
        CatalogueCompetences.parFamille[FamilleCompetence.COMBAT_MELEE]
            ?.filter { it.appartientAuTronc == null }
            ?.forEach { comp -> items.add(construireItemIndividuel(comp, voyageur)) }
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

    private fun construireItemIndividuel(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel)
        )
    }
}

sealed interface CompetencesUiState {
    data object Loading : CompetencesUiState
    data class Success(
        val doubleListes: List<DoubleListe>,
        val pointsRestants: Int,
        val hautRevant: Boolean
    ) : CompetencesUiState
}

data class DoubleListe(
    val gauche: ListeCompetences,
    val droite: ListeCompetences
)

data class ListeCompetences(
    val famille: FamilleCompetence,
    val items: List<CompetenceUiItem>
)

sealed interface CompetenceUiItem {
    data class Individuelle(
        val competence: Competence,
        val niveauActuel: Int,
        val coutCumule: Int
    ) : CompetenceUiItem

    data class Separateur(val labelRes: Int) : CompetenceUiItem
}
