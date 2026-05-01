package com.jb.voyageur.feature.accueil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.usecase.CreerVoyageurUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccueilViewModel @Inject constructor(
    private val creerVoyageurUseCase: CreerVoyageurUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccueilUiState())
    val uiState: StateFlow<AccueilUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<AccueilNavigation>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()

    fun onNomChange(nom: String) {
        _uiState.update { it.copy(nom = nom) }
    }

    fun onSexeChange(sexe: Sexe) {
        _uiState.update { it.copy(sexe = sexe) }
    }

    fun onHautRevantChange(hautRevant: Boolean) {
        _uiState.update { it.copy(hautRevant = hautRevant) }
    }

    fun onCreerVoyageur() {
        viewModelScope.launch {
            val state = _uiState.value
            val id = creerVoyageurUseCase(
                nom = state.nom,
                sexe = state.sexe,
                hautRevant = state.hautRevant
            )
            _navigation.send(AccueilNavigation.VersCaracteristiques(id))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _navigation.close()
    }
}

data class AccueilUiState(
    val nom: String = "",
    val sexe: Sexe = Sexe.HOMME,
    val hautRevant: Boolean = false
)

sealed interface AccueilNavigation {
    data class VersCaracteristiques(val voyageurId: Long) : AccueilNavigation
}
