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

    fun onCompetenceChange(nom: String, borneInf: Int, niveauCible: Int) {
        viewModelScope.launch {
            modifierCompetenceUseCase(voyageurId, nom, borneInf, niveauCible)
        }
    }

    fun onTroncChange(nomTronc: String, membre: String, niveauCible: Int) {
        viewModelScope.launch {
            modifierNiveauTroncUseCase(voyageurId, nomTronc, membre, niveauCible)
        }
    }

    fun onDraconicChange(voie: VoieDraconic, niveauCible: Int) {
        viewModelScope.launch {
            modifierDraconicUseCase(voyageurId, voie, niveauCible)
        }
    }

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

        result.add(ColonneCompetences(
            famille = FamilleCompetence.GENERALE,
            items = buildItems(FamilleCompetence.GENERALE, voyageur)
        ))

        result.add(ColonneCompetences(
            famille = FamilleCompetence.PARTICULIERE,
            items = buildItems(FamilleCompetence.PARTICULIERE, voyageur)
        ))

        result.add(ColonneCompetences(
            famille = FamilleCompetence.COMBAT_MELEE,
            items = buildItemsCombat(voyageur)
        ))

        result.add(ColonneCompetences(
            famille = FamilleCompetence.TIR_LANCER,
            items = buildItems(FamilleCompetence.TIR_LANCER, voyageur)
        ))

        result.add(ColonneCompetences(
            famille = FamilleCompetence.CONNAISSANCE,
            items = buildItems(FamilleCompetence.CONNAISSANCE, voyageur)
        ))

        result.add(ColonneCompetences(
            famille = FamilleCompetence.SPECIALISEE,
            items = buildItems(FamilleCompetence.SPECIALISEE, voyageur)
        ))

        if (voyageur.hautRevant) {
            result.add(ColonneCompetences(
                famille = FamilleCompetence.DRACONIC,
                items = buildItemsDraconic(voyageur)
            ))
        }

        return result
    }

    private fun buildItems(famille: FamilleCompetence, voyageur: Voyageur): List<CompetenceUiItem.Individuelle> {
        return CatalogueCompetences.parFamille[famille]?.map { comp ->
            if (comp.nom in CatalogueCompetences.SURVIES_SPECIFIQUES) {
                construireItemSurvie(comp, voyageur)
            } else {
                construireItemIndividuel(comp, voyageur)
            }
        } ?: emptyList()
    }

    private fun buildItemsCombat(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> {
        val items = mutableListOf<CompetenceUiItem.Individuelle>()

        // TroncCorps
        val corpsEstSepare = voyageur.troncCorps.estSepare
        voyageur.troncCorps.membres.forEachIndexed { index, nom ->
            val niveau = voyageur.troncCorps.niveauPour(nom)
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncCorps")

            val cout = if (index == 0) voyageur.troncCorps.coutTotal() else 0
            val borneInf = if (corpsEstSepare) 0 else -6

            items.add(CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = cout,
                borneInf = borneInf,
                borneSup = 3,
                appartientAuTronc = "TroncCorps",
                estPremierDuTronc = index == 0
            ))
        }

        // TroncArmes
        val armesEstSepare = voyageur.troncArmes.estSepare
        voyageur.troncArmes.membres.forEachIndexed { index, nom ->
            val niveau = voyageur.troncArmes.niveauPour(nom)
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncArmes")

            val cout = if (index == 0) voyageur.troncArmes.coutTotal() else 0
            val borneInf = if (armesEstSepare) 0 else -6

            items.add(CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = cout,
                borneInf = borneInf,
                borneSup = 3,
                appartientAuTronc = "TroncArmes",
                estPremierDuTronc = index == 0
            ))
        }

        CatalogueCompetences.parFamille[FamilleCompetence.COMBAT_MELEE]
            ?.filter { it.appartientAuTronc == null }
            ?.forEach { comp ->
                items.add(construireItemIndividuel(comp, voyageur))
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
            CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = CoutCompetence.coutCumuleAvecMultiplicateur(-11, niveau, multiplicateur),
                borneInf = -11,
                borneSup = 3
            )
        }

    private fun construireItemIndividuel(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
            borneInf = competence.niveauBase,
            borneSup = 3
        )
    }

    private fun construireItemSurvie(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        val survieExterieur = voyageur.competences["Survie en extérieur"] ?: -8
        val borneSup = if (survieExterieur >= 0) 3 else survieExterieur

        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
            borneInf = competence.niveauBase,
            borneSup = borneSup
        )
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
        val coutCumule: Int,
        val borneInf: Int = competence.niveauBase,
        val borneSup: Int = 3,
        val appartientAuTronc: String? = null,
        val estPremierDuTronc: Boolean = false
    ) : CompetenceUiItem
}
