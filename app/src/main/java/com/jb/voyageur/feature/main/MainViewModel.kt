package com.jb.voyageur.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.domain.usecase.ModifierDescriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val modifierDescriptionUseCase: ModifierDescriptionUseCase
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    val voyageurNom: StateFlow<String> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { it.nom }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    fun onRename(nouveauNom: String) {
        viewModelScope.launch {
            modifierDescriptionUseCase(voyageurId, ChampDescription.NOM, nouveauNom)
        }
    }
}
