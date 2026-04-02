package com.timeforyou.app.data.profile

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

@Singleton
class ProfilePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    private val _displayName = MutableStateFlow(readDisplayName())
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    fun setDisplayName(raw: String) {
        val normalized = raw.trim()
        prefs.edit { putString(KEY_DISPLAY_NAME, normalized) }
        _displayName.value = normalized
    }

    private fun readDisplayName(): String =
        prefs.getString(KEY_DISPLAY_NAME, null)?.trim().orEmpty()

    companion object {
        private const val PREFS_NAME = "time_for_you_profile"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
}
