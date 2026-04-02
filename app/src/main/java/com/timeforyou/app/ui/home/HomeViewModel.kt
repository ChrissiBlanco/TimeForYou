package com.timeforyou.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.repository.TimeRepository
import com.timeforyou.app.domain.usecase.home.BuildHomeDashboardSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    /** Today’s logs, newest first. */
    val todaysMoments: List<BehaviorLog> = emptyList(),
    /** Consecutive days with at least one log (0 if none). */
    val streak: Int = 0,
    /** Streak would reset if today ends with no log (streak > 0 but nothing logged today). */
    val streakAtRisk: Boolean = false,
    /** Three chips in the log dialog: from past notes, padded with defaults. */
    val momentSuggestions: List<String> = emptyList(),
    val headline: String = "Time for you",
    val subtitle: String = "Log a mindful moment today.",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TimeRepository,
    private val buildHomeDashboardSnapshot: BuildHomeDashboardSnapshotUseCase,
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Bumps when the screen resumes so [java.time.LocalDate.now] and week buckets recompute without a DB write. */
    private val refreshOnResume = MutableStateFlow(0)

    /** Bumps at each local midnight so "today" stays correct if the app stays foregrounded. */
    private val midnightTick = MutableStateFlow(0)

    init {
        viewModelScope.launch { runLocalMidnightTicker() }
        viewModelScope.launch {
            combine(
                repository.observeLastSevenDays(),
                repository.observeLogs(),
                repository.observeStreak(),
                refreshOnResume,
                midnightTick,
            ) { days, logs, streak, _, _ ->
                Triple(days, logs, streak)
            }.collect { (days, logs, streak) ->
                val snapshot = buildHomeDashboardSnapshot(days, logs, streak)
                _uiState.update {
                    it.copy(
                        todaysMoments = snapshot.todaysMoments,
                        streak = streak,
                        streakAtRisk = snapshot.streakAtRisk,
                        momentSuggestions = snapshot.momentSuggestions,
                        subtitle = snapshot.subtitle,
                    )
                }
            }
        }
    }

    fun onResume() {
        refreshOnResume.update { it + 1 }
        repository.refreshCalendarWindow()
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
