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
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val defaultMomentSuggestions = listOf(
    "Short walk",
    "Breathing pause",
    "Stretch break",
)

data class HomeUiState(
    /** True once week data is loaded and today (last bucket) has no logs yet. */
    val needsTodayLogReminder: Boolean = false,
    /** Today’s logs, newest first. */
    val todaysMoments: List<BehaviorLog> = emptyList(),
    /** Consecutive days with at least one log (0 if none). */
    val streak: Int = 0,
    /** Three chips in the log dialog: from past notes, padded with defaults. */
    val momentSuggestions: List<String> = defaultMomentSuggestions,
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
                repository.observeStreak(),
                refreshOnResume,
                midnightTick,
            ) { days, logs, streak, _, _ ->
                Triple(days, logs, streak)
            }.collect { (days, logs, streak) ->
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
                        streak = streak,
                        momentSuggestions = buildMomentSuggestions(logs),
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

    private fun buildMomentSuggestions(logs: List<BehaviorLog>): List<String> {
        val fromHistory = logs
            .asSequence()
            .sortedByDescending { it.timestampEpochMillis }
            .mapNotNull { it.note?.trim()?.takeIf { n -> n.isNotEmpty() } }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .map { truncateForChip(it) }
            .take(3)
            .toList()
        if (fromHistory.size >= 3) {
            return fromHistory.take(3)
        }
        val usedLower = fromHistory.map { it.lowercase(Locale.getDefault()) }.toMutableSet()
        val result = fromHistory.toMutableList()
        for (fallback in defaultMomentSuggestions) {
            if (result.size >= 3) break
            val key = fallback.lowercase(Locale.getDefault())
            if (key !in usedLower) {
                result.add(fallback)
                usedLower.add(key)
            }
        }
        return result.take(3)
    }

    private fun truncateForChip(note: String): String {
        val max = 40
        return if (note.length <= max) note else note.take(max - 1) + "…"
    }
}
