package com.timeforyou.app.domain.usecase

import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.DayAggregate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class BuildCoachActivitySummaryUseCase @Inject constructor() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    operator fun invoke(
        streak: Int,
        todayLogCount: Int,
        logs: List<BehaviorLog>,
        lastSevenDays: List<DayAggregate>,
        displayName: String,
    ): CoachActivitySummary {
        val excerpts = logs
            .asSequence()
            .sortedByDescending { it.timestampEpochMillis }
            .mapNotNull { it.note?.trim()?.takeIf { n -> n.isNotEmpty() } }
            .take(MAX_EXCERPTS)
            .map { truncateNote(it, EXCERPT_MAX_CHARS) }
            .toList()
        val timeDesc = typicalLogTimeDescription(logs)
        return CoachActivitySummary(
            streak = streak,
            todayLogCount = todayLogCount,
            daysWithActivityLast7 = lastSevenDays.count { it.logCount > 0 },
            totalLogsLast7 = lastSevenDays.sumOf { it.logCount },
            recentNoteExcerpts = excerpts,
            typicalLogTimeDescription = timeDesc,
            displayName = displayName.trim().ifBlank { "You" },
        )
    }

    private fun typicalLogTimeDescription(logs: List<BehaviorLog>): String {
        val today = LocalDate.now(zoneId)
        val windowStart = today.minusDays(6)
        val inWindow = logs.filter { log ->
            val d = Instant.ofEpochMilli(log.timestampEpochMillis).atZone(zoneId).toLocalDate()
            !d.isBefore(windowStart) && !d.isAfter(today)
        }
        if (inWindow.isEmpty()) {
            return "No moments in the last 7 days yet—your first log will show when you usually pause."
        }
        fun bucket(hour: Int): String =
            when (hour) {
                in 5..11 -> "morning"
                in 12..16 -> "afternoon"
                in 17..21 -> "evening"
                else -> "late night or very early morning"
            }
        val counts = inWindow
            .groupingBy {
                bucket(
                    Instant.ofEpochMilli(it.timestampEpochMillis).atZone(zoneId).hour,
                )
            }
            .eachCount()
        val sorted = counts.entries.sortedByDescending { it.value }
        val top = sorted[0]
        val second = sorted.getOrNull(1)
        val n = inWindow.size
        val dominant = top.value.toFloat() / n
        return when {
            dominant >= 0.55f ->
                "Recently you most often log in the ${top.key}."
            second != null && second.value * 2 >= top.value ->
                "Lately your logs are spread across ${top.key} and ${second.key}."
            else ->
                "Your recent logs vary by time—${top.key} shows up most often."
        }
    }

    private fun truncateNote(s: String, max: Int): String =
        if (s.length <= max) s else s.take(max - 1) + "…"

    private companion object {
        const val MAX_EXCERPTS = 5
        const val EXCERPT_MAX_CHARS = 72
    }
}
