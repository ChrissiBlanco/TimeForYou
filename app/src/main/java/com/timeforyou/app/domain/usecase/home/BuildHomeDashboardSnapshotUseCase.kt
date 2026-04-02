package com.timeforyou.app.domain.usecase.home

import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.domain.model.HomeDashboardSnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

class BuildHomeDashboardSnapshotUseCase @Inject constructor() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    operator fun invoke(
        lastSevenDays: List<DayAggregate>,
        logs: List<BehaviorLog>,
        streak: Int,
    ): HomeDashboardSnapshot {
        val today = LocalDate.now(zoneId)
        val todaysMoments = logs
            .filter { log ->
                Instant.ofEpochMilli(log.timestampEpochMillis)
                    .atZone(zoneId)
                    .toLocalDate() == today
            }
            .sortedByDescending { it.timestampEpochMillis }
        val streakAtRisk = streak > 0 && todaysMoments.isEmpty()
        val reminder = lastSevenDays.isNotEmpty() && lastSevenDays.last().logCount == 0
        val subtitle =
            if (reminder) {
                "There’s space here for you whenever you’re ready."
            } else {
                "So glad you found a moment today."
            }
        return HomeDashboardSnapshot(
            todaysMoments = todaysMoments,
            momentSuggestions = buildMomentSuggestions(logs),
            streakAtRisk = streakAtRisk,
            subtitle = subtitle,
        )
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
        for (fallback in DEFAULT_MOMENT_SUGGESTIONS) {
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

    private companion object {
        private val DEFAULT_MOMENT_SUGGESTIONS = listOf(
            "Short walk",
            "Breathing pause",
            "Stretch break",
        )
    }
}