package com.jb.voyageur.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.ChampDescription
import com.jb.voyageur.core.domain.usecase.ExportVoyageurPdfUseCase
import com.jb.voyageur.core.domain.usecase.ModifierDescriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val modifierDescriptionUseCase: ModifierDescriptionUseCase,
    private val exportVoyageurPdfUseCase: ExportVoyageurPdfUseCase
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

    val hautRevant: StateFlow<Boolean> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { it.hautRevant }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState = _pdfExportState.asStateFlow()

    fun onRename(nouveauNom: String) {
        viewModelScope.launch {
            modifierDescriptionUseCase(voyageurId, ChampDescription.NOM, nouveauNom)
        }
    }

    fun onExportPdf() {
        viewModelScope.launch {
            _pdfExportState.value = PdfExportState.Loading
            val pdfData = exportVoyageurPdfUseCase(voyageurId)
            if (pdfData != null) {
                _pdfExportState.value = PdfExportState.Success(pdfData, voyageurNom.value.ifBlank { "Voyageur" } + ".pdf")
            } else {
                _pdfExportState.value = PdfExportState.Error("Erreur lors de la génération du PDF")
            }
        }
    }

    fun onPdfExportConsumed() {
        _pdfExportState.value = PdfExportState.Idle
    }
}

sealed interface PdfExportState {
    data object Idle : PdfExportState
    data object Loading : PdfExportState
    data class Success(val data: ByteArray, val fileName: String) : PdfExportState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            if (!data.contentEquals(other.data)) return false
            if (fileName != other.fileName) return false
            return true
        }
        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + fileName.hashCode()
            return result
        }
    }
    data class Error(val message: String) : PdfExportState
}
