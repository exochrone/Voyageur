package com.jb.voyageur.feature.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.data.DebugSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionsViewModel @Inject constructor(
    private val debugSettingsManager: DebugSettingsManager
) : ViewModel() {

    val backgroundImageUri: StateFlow<String?> = debugSettingsManager.backgroundImageUri

    fun onBackgroundImageSelected(uri: String?) {
        viewModelScope.launch {
            debugSettingsManager.setBackgroundImageUri(uri)
        }
    }
}
