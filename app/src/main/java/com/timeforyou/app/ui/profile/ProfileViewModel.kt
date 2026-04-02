package com.timeforyou.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.data.profile.ProfilePreferences
import com.timeforyou.app.domain.repository.TimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val clearedMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TimeRepository,
    private val profilePreferences: ProfilePreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profilePreferences.displayName.collect { name ->
                _uiState.update { it.copy(displayName = name) }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        profilePreferences.setDisplayName(value)
        _uiState.update { it.copy(clearedMessage = null) }
    }

    fun onClearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.update {
                it.copy(clearedMessage = "Your local logs were cleared.")
            }
        }
    }
}
