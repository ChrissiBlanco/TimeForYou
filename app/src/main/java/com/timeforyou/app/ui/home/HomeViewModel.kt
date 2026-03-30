package com.timeforyou.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.repository.TimeRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    /** True once week data is loaded and today (last bucket) has no logs yet. */
    val needsTodayLogReminder: Boolean = false,
    /** Today’s logs, newest first. */
    val todaysMoments: List<BehaviorLog> = emptyList(),
    val headline: String = "Time for you",
    val subtitle: String = "Log a mindful moment today.",
)

class HomeViewModel(
    private val repository: TimeRepository,
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeLastSevenDays(),
                repository.observeLogs(),
            ) { days, logs ->
                Pair(days, logs)
            }.collect { (days, logs) ->
                val reminder = days.isNotEmpty() && days.last().logCount == 0
                val today = LocalDate.now(zoneId)
                val todaysMoments = logs
                    .filter { log ->
                        Instant.ofEpochMilli(log.timestampEpochMillis)
                            .atZone(zoneId)
                            .toLocalDate() == today
                    }
                    .sortedByDescending { it.timestampEpochMillis }
                _uiState.update {
                    it.copy(
                        needsTodayLogReminder = reminder,
                        todaysMoments = todaysMoments,
                        subtitle = if (reminder) {
                            "There’s space here for you whenever you’re ready."
                        } else {
                            "So glad you found a moment today."
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
