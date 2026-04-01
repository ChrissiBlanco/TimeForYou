package com.timeforyou.app.domain.usecase

import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.DayAggregate
import javax.inject.Inject

class BuildCoachActivitySummaryUseCase @Inject constructor() {

    operator fun invoke(
        streak: Int,
        todayLogCount: Int,
        logs: List<BehaviorLog>,
        lastSevenDays: List<DayAggregate>,
    ): CoachActivitySummary {
        val excerpts = logs
            .asSequence()
            .sortedByDescending { it.timestampEpochMillis }
            .mapNotNull { it.note?.trim()?.takeIf { n -> n.isNotEmpty() } }
            .take(MAX_EXCERPTS)
            .map { truncateNote(it, EXCERPT_MAX_CHARS) }
            .toList()
        return CoachActivitySummary(
            streak = streak,
            todayLogCount = todayLogCount,
            daysWithActivityLast7 = lastSevenDays.count { it.logCount > 0 },
            totalLogsLast7 = lastSevenDays.sumOf { it.logCount },
            recentNoteExcerpts = excerpts,
        )
    }

    private fun truncateNote(s: String, max: Int): String =
        if (s.length <= max) s else s.take(max - 1) + "…"

    private companion object {
        const val MAX_EXCERPTS = 5
        const val EXCERPT_MAX_CHARS = 72
    }
}
