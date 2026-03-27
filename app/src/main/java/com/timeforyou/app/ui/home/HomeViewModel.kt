package com.timeforyou.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.data.repository.TimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val streak: Int = 0,
    val todayLogCount: Int = 0,
    val headline: String = "Time for you",
    val subtitle: String = "Log a mindful moment today.",
)

class HomeViewModel(
    private val repository: TimeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeStreak(),
                repository.observeTodayLogCount(),
            ) { streak, today ->
                Pair(streak, today)
            }.collect { (streak, today) ->
                _uiState.update {
                    it.copy(
                        streak = streak,
                        todayLogCount = today,
                        subtitle = when {
                            today == 0 -> "Tap below when you take a moment for yourself."
                            today == 1 -> "You’ve logged once today. Beautiful."
                            else -> "You’ve shown up $today times today."
                        },
                    )
                }
            }
        }
    }

    fun logMoment(note: String?, timestampEpochMillis: Long?) {
        viewModelScope.launch {
            val trimmed = note?.trim()?.takeIf { it.isNotEmpty() }
            repository.logMoment(
                category = "moment",
                note = trimmed,
                timestampEpochMillis = timestampEpochMillis,
            )
        }
    }
}
