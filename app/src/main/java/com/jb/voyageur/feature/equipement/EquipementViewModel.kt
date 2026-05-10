package com.jb.voyageur.feature.equipement

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.R
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.AcheterObjetUseCase
import com.jb.voyageur.core.domain.usecase.RembourserObjetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EquipementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val acheterObjetUseCase: AcheterObjetUseCase,
    private val rembourserObjetUseCase: RembourserObjetUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    private val catalogue: List<CategorieEquipement> by lazy {
        CatalogueEquipement.charger(context.assets)
    }

    val uiState: StateFlow<EquipementUiState> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { voyageur -> voyageur.toEquipementUiState() }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = EquipementUiState.Loading
        )

    private val _messageErreur = MutableStateFlow<Int?>(null)
    val messageErreur: StateFlow<Int?> = _messageErreur.asStateFlow()

    fun onAcheter(objet: ObjetEquipement) {
        viewModelScope.launch {
            when (acheterObjetUseCase(voyageurId, objet)) {
                AcheterObjetUseCase.Resultat.Succes             -> { /* Flow met à jour l'UI */ }
                AcheterObjetUseCase.Resultat.FortuneInsuffisante ->
                    _messageErreur.value = R.string.equipement_fortune_insuffisante
            }
        }
    }

    fun onRembourser(nomObjet: String) {
        viewModelScope.launch {
            rembourserObjetUseCase(voyageurId, nomObjet)
        }
    }

    fun effacerErreur() { _messageErreur.value = null }

    private fun Voyageur.toEquipementUiState(): EquipementUiState.Success {
        val possedes = equipement.sortedBy { it.nom }

        // Colonne "Équipement possédé" — groupée par catégorie pour l'affichage
        val possedesParCategorie = possedes
            .groupBy { objet ->
                catalogue.firstOrNull { cat ->
                    cat.objets.any { it.nom == objet.nom }
                }?.nom ?: "Divers"
            }
            .entries
            .sortedBy { (cat, _) -> catalogue.indexOfFirst { it.nom == cat } }
            .map { (cat, objets) -> cat to objets }

        val colonnesPossedes = ColonneEquipement.Possedes(
            groupes  = possedesParCategorie,
            fortune  = fortune,
            encTotal = possedes.sumOf { it.encombrementTotal.toDouble() }.toFloat()
        )

        val colonnesCatalogue = catalogue.map { categorie ->
            ColonneEquipement.Catalogue(
                categorie   = categorie,
                nomsAchetes = possedes.map { it.nom }.toSet()
            )
        }

        return EquipementUiState.Success(
            colonnePossedes  = colonnesPossedes,
            colonnesCatalogue = colonnesCatalogue,
            fortune          = fortune,
            encMax           = caracteristiques.encombrement,
            hautRevant       = hautRevant
        )
    }
}

sealed interface EquipementUiState {
    data object Loading : EquipementUiState
    data class Success(
        val colonnePossedes:   ColonneEquipement.Possedes,
        val colonnesCatalogue: List<ColonneEquipement.Catalogue>,
        val fortune:           Int,
        val encMax:            Float,
        val hautRevant:        Boolean
    ) : EquipementUiState
}

sealed interface ColonneEquipement {
    data class Possedes(
        val groupes: List<Pair<String, List<ObjetPossede>>>,
        val fortune: Int,
        val encTotal: Float
    ) : ColonneEquipement

    data class Catalogue(
        val categorie:   CategorieEquipement,
        val nomsAchetes: Set<String>
    ) : ColonneEquipement
}
