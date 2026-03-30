package com.timeforyou.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.repository.TimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoachTip(
    val title: String,
    val body: String,
)

data class CoachUiState(
    val tips: List<CoachTip> = defaultTips,
    val streak: Int = 0,
    val headerMessage: String = "Small steps, gentle pace.",
)

private val defaultTips = listOf(
    CoachTip(
        title = "Two-minute reset",
        body = "Set a timer, soften your shoulders, and notice three breaths without changing them.",
    ),
    CoachTip(
        title = "Behavior, not judgment",
        body = "Tracking is a mirror, not a scorecard. Curiosity beats criticism every time.",
    ),
    CoachTip(
        title = "Anchor habit",
        body = "Link your log to something you already do—after coffee, after closing the laptop, after a walk.",
    ),
    CoachTip(
        title = "Evening reflection",
        body = "Name one moment you were kind to yourself today, however small.",
    ),
)

class CoachViewModel(
    private val repository: TimeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeStreak(),
                repository.observeTodayLogCount(),
            ) { streak, today ->
                Pair(streak, today)
            }.collect { (streak, today) ->
                _uiState.update {
                    val message = when {
                        streak >= 7 -> "Your streak is glowing—rest is part of the practice too."
                        streak >= 3 -> "Consistency is showing up. Keep the rhythm gentle."
                        today > 0 -> "You’ve tended to yourself today. That matters."
                        else -> "Small steps, gentle pace."
                    }
                    it.copy(streak = streak, headerMessage = message)
                }
            }
        }
    }


}
