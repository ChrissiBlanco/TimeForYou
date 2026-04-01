package com.timeforyou.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.repository.TimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TimeRepository,
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Bumps when the screen resumes so [LocalDate.now] and week buckets recompute without a DB write. */
    private val refreshOnResume = MutableStateFlow(0)

    /** Bumps at each local midnight so "today" stays correct if the app stays foregrounded. */
    private val midnightTick = MutableStateFlow(0)

    init {
        viewModelScope.launch { runLocalMidnightTicker() }
        viewModelScope.launch {
            combine(
                repository.observeLastSevenDays(),
                repository.observeLogs(),
                refreshOnResume,
                midnightTick,
            ) { days, logs, _, _ ->
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

    fun onResume() {
        refreshOnResume.update { it + 1 }
    }

    /**
     * Waits until the next start of day in [zoneId], then increments [midnightTick]. Repeats until cancelled.
     */
    private suspend fun runLocalMidnightTicker() {
        while (true) {
            val now = Instant.now()
            val nextMidnight = now.atZone(zoneId)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()
            val millis = Duration.between(now, nextMidnight).toMillis()
            if (millis > 0) {
                delay(millis)
            }
            midnightTick.update { it + 1 }
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
