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
import com.jb.voyageur.core.domain.usecase.MettreAJourPhysiqueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
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
    private val mettreAJourPhysiqueUseCase: MettreAJourPhysiqueUseCase,
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

    private val _aideActive = MutableStateFlow<ChampAffichage?>(null)
    val aideActive: StateFlow<ChampAffichage?> = _aideActive.asStateFlow()

    init {
        // Observer les changements de TAILLE et Sexe pour régénérer
        viewModelScope.launch {
            uiState
                .filterIsInstance<CaracteristiquesUiState.Success>()
                .scan<CaracteristiquesUiState.Success, Pair<CaracteristiquesUiState.Success?, CaracteristiquesUiState.Success?>>(
                    Pair(null, null)
                ) { acc, new -> Pair(acc.second, new) }
                .collect { (prev, curr) ->
                    if (prev != null && curr != null &&
                        (prev.caracteristiques.taille != curr.caracteristiques.taille ||
                         prev.sexe != curr.sexe)
                    ) {
                        mettreAJourPhysiqueUseCase.generer(voyageurId)
                    }
                }
        }
    }

    fun onCaracteristiqueChange(champ: ChampCaracteristique, valeur: Int) {
        viewModelScope.launch {
            modifierCaracteristiqueUseCase(voyageurId, champ, valeur)
        }
    }

    fun onBeauteChange(valeur: Int) {
        viewModelScope.launch {
            modifierBeauteUseCase(voyageurId, valeur)
        }
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

    fun onPoidsSaisi(valeur: Int) {
        viewModelScope.launch {
            mettreAJourPhysiqueUseCase.clamperPoids(voyageurId, valeur)
        }
    }

    fun onTailleCmSaisie(valeur: Int) {
        viewModelScope.launch {
            mettreAJourPhysiqueUseCase.clamperTailleCm(voyageurId, valeur)
        }
    }

    fun onDemanderAide(champ: ChampAffichage) {
        _aideActive.value = champ
    }

    fun onFermerAide() {
        _aideActive.value = null
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
        val hautRevant: Boolean,
        val aDesSortsAccessibles: Boolean,
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
    val aDesSorts = hautRevant && (
        draconic.oniros > -11 || 
        draconic.hypnos > -11 || 
        draconic.narcos > -11 || 
        draconic.thanatos > -11
    )
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
        hautRevant = hautRevant,
        aDesSortsAccessibles = aDesSorts,
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
