package com.jb.voyageur.feature.caracteristiques

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.Caracteristiques
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.model.melee
import com.jb.voyageur.core.domain.model.tir
import com.jb.voyageur.core.domain.model.lancer
import com.jb.voyageur.core.domain.model.derobee
import com.jb.voyageur.core.domain.model.pointsDeVie
import com.jb.voyageur.core.domain.model.endurance
import com.jb.voyageur.core.domain.model.seuilConstitution
import com.jb.voyageur.core.domain.model.sustentation
import com.jb.voyageur.core.domain.model.bonusDommages
import com.jb.voyageur.core.domain.model.encombrement
import com.jb.voyageur.core.domain.model.pointsTotal
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.ChampCaracteristique
import com.jb.voyageur.core.domain.usecase.ModifierCaracteristiqueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaracteristiquesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val modifierCaracteristiqueUseCase: ModifierCaracteristiqueUseCase,
    private val voyageurRepository: VoyageurRepository
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    val uiState: StateFlow<CaracteristiquesUiState> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { voyageur -> voyageur.toCaracteristiquesUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CaracteristiquesUiState.Loading
        )

    fun onCaracteristiqueChange(champ: ChampCaracteristique, valeur: Int) {
        viewModelScope.launch {
            modifierCaracteristiqueUseCase(voyageurId, champ, valeur)
        }
    }
}

sealed interface CaracteristiquesUiState {
    data object Loading : CaracteristiquesUiState
    data class Success(
        val nom: String,
        val caracteristiques: Caracteristiques,
        val beaute: Int,
        val pointsRestants: Int,
        val derobee: Int,
        val melee: Int,
        val tir: Int,
        val lancer: Int,
        val vie: Int,
        val endurance: Int,
        val sc: Int,
        val sust: Int,
        val bonusDom: Int,
        val encombrement: Float,
        val forceMax: Int
    ) : CaracteristiquesUiState
}

private fun Voyageur.toCaracteristiquesUiState(): CaracteristiquesUiState.Success {
    val pointsBeaute = (beaute - 10).coerceAtLeast(0)
    return CaracteristiquesUiState.Success(
        nom = nom,
        caracteristiques = caracteristiques,
        beaute = beaute,
        pointsRestants = 160 - caracteristiques.pointsTotal - pointsBeaute,
        derobee = caracteristiques.derobee,
        melee = caracteristiques.melee,
        tir = caracteristiques.tir,
        lancer = caracteristiques.lancer,
        vie = caracteristiques.pointsDeVie,
        endurance = caracteristiques.endurance,
        sc = caracteristiques.seuilConstitution,
        sust = caracteristiques.sustentation,
        bonusDom = caracteristiques.bonusDommages,
        encombrement = caracteristiques.encombrement,
        forceMax = (caracteristiques.taille + 4).coerceAtMost(15)
    )
}
