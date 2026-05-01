package com.jb.voyageur.feature.caracteristiques

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.Caracteristiques
import com.jb.voyageur.core.domain.model.HeureNaissance
import com.jb.voyageur.core.domain.model.Lateralite
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.model.Voyageur
import com.jb.voyageur.core.domain.model.bonusDommages
import com.jb.voyageur.core.domain.model.derobee
import com.jb.voyageur.core.domain.model.encombrement
import com.jb.voyageur.core.domain.model.endurance
import com.jb.voyageur.core.domain.model.lancer
import com.jb.voyageur.core.domain.model.melee
import com.jb.voyageur.core.domain.model.pointsDeVie
import com.jb.voyageur.core.domain.model.pointsTotal
import com.jb.voyageur.core.domain.model.seuilConstitution
import com.jb.voyageur.core.domain.model.sustentation
import com.jb.voyageur.core.domain.model.tir
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.ChampCaracteristique
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.domain.usecase.ModifierBeauteUseCase
import com.jb.voyageur.core.domain.usecase.ModifierCaracteristiqueUseCase
import com.jb.voyageur.core.domain.usecase.ModifierDescriptionUseCase
import com.jb.voyageur.core.domain.usecase.ModifierHeureNaissanceUseCase
import com.jb.voyageur.core.domain.usecase.ModifierLateraliteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaracteristiquesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val modifierCaracteristiqueUseCase: ModifierCaracteristiqueUseCase,
    private val modifierBeauteUseCase: ModifierBeauteUseCase,
    private val modifierDescriptionUseCase: ModifierDescriptionUseCase,
    private val modifierHeureNaissanceUseCase: ModifierHeureNaissanceUseCase,
    private val modifierLateraliteUseCase: ModifierLateraliteUseCase,
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

    private val _aideActive = MutableStateFlow<ChampAide?>(null)
    val aideActive: StateFlow<ChampAide?> = _aideActive.asStateFlow()

    private val _events = Channel<CaracteristiquesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var confirmationBeauteEnAttente: Int? = null

    fun onCaracteristiqueChange(champ: ChampCaracteristique, valeur: Int) {
        viewModelScope.launch {
            modifierCaracteristiqueUseCase(voyageurId, champ, valeur)
        }
    }

    fun onBeauteChange(valeur: Int) {
        viewModelScope.launch {
            val resultat = modifierBeauteUseCase(voyageurId, valeur)
            when (resultat) {
                is ModifierBeauteUseCase.Resultat.ConfirmationRequise -> {
                    confirmationBeauteEnAttente = valeur
                    _events.send(CaracteristiquesEvent.ConfirmerPerteBeaute(resultat.pointsPerdus))
                }
                ModifierBeauteUseCase.Resultat.Succes -> { }
            }
        }
    }

    fun onConfirmerPerteBeaute() {
        val valeur = confirmationBeauteEnAttente ?: return
        confirmationBeauteEnAttente = null
        viewModelScope.launch {
            modifierBeauteUseCase(voyageurId, valeur, confirmationAcceptee = true)
        }
    }

    fun onAnnulerPerteBeaute() {
        confirmationBeauteEnAttente = null
    }

    fun onDescriptionChange(champ: ChampDescription, valeur: String) {
        viewModelScope.launch {
            modifierDescriptionUseCase(voyageurId, champ, valeur)
        }
    }

    fun onHeureNaissanceChange(heure: HeureNaissance) {
        viewModelScope.launch {
            modifierHeureNaissanceUseCase(voyageurId, heure)
        }
    }

    fun onLateraliteChange(lateralite: Lateralite) {
        viewModelScope.launch {
            modifierLateraliteUseCase(voyageurId, lateralite)
        }
    }

    fun onDemanderAide(champ: ChampAide) {
        _aideActive.value = champ
    }

    fun onFermerAide() {
        _aideActive.value = null
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}

sealed interface CaracteristiquesUiState {
    data object Loading : CaracteristiquesUiState
    data class Success(
        val nom: String,
        val sexe: Sexe,
        val age: Int?,
        val tailleCm: Int?,
        val poidsKg: Int?,
        val cheveux: String,
        val yeux: String,
        val signeParticulier: String,
        val lateralite: Lateralite,
        val heureNaissance: HeureNaissance,
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

sealed interface CaracteristiquesEvent {
    data class ConfirmerPerteBeaute(val pointsPerdus: Int) : CaracteristiquesEvent
}

sealed interface ChampAide {
    data class Carac(val champ: ChampCaracteristique) : ChampAide
    data object Beaute : ChampAide
}

private fun Voyageur.toCaracteristiquesUiState(): CaracteristiquesUiState.Success {
    val pointsBeaute = (beaute - 10).coerceAtLeast(0)
    return CaracteristiquesUiState.Success(
        nom = nom,
        sexe = sexe,
        age = age,
        tailleCm = tailleCm,
        poidsKg = poidsKg,
        cheveux = cheveux,
        yeux = yeux,
        signeParticulier = signeParticulier,
        lateralite = lateralite,
        heureNaissance = heureNaissance,
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
