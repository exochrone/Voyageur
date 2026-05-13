package com.jb.voyageur.core.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("debug_settings", Context.MODE_PRIVATE)
    
    private val _backgroundImageUri = MutableStateFlow(prefs.getString("background_image_uri", null))
    val backgroundImageUri: StateFlow<String?> = _backgroundImageUri

    fun setBackgroundImageUri(uri: String?) {
        prefs.edit().putString("background_image_uri", uri).apply()
        _backgroundImageUri.value = uri
    }
}
