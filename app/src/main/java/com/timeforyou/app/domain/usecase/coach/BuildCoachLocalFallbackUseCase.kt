package com.timeforyou.app.domain.usecase.coach

import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachTip
import javax.inject.Inject

class BuildCoachLocalFallbackUseCase @Inject constructor() {

    fun insightSummary(summary: CoachActivitySummary): String {
        val rawName = summary.displayName.trim()
        val useName = rawName.isNotEmpty()
        val streakPart =
            when {
                summary.streak <= 0 ->
                    if (useName) {
                        "$rawName, you're starting fresh—any single log is a win."
                    } else {
                        "You're starting fresh—any single log is a win."
                    }
                summary.streak == 1 ->
                    if (useName) {
                        "$rawName, you've logged at least one day in a row."
                    } else {
                        "You've logged at least one day in a row."
                    }
                else ->
                    if (useName) {
                        "$rawName, you're on a ${summary.streak}-day logging streak."
                    } else {
                        "You're on a ${summary.streak}-day logging streak."
                    }
            }
        return "$streakPart ${summary.typicalLogTimeDescription}"
    }

    fun tips(summary: CoachActivitySummary): List<CoachTip> {
        val tips = mutableListOf<CoachTip>()
        if (summary.totalLogsLast7 == 0) {
            tips.add(
                CoachTip(
                    title = "First gentle log",
                    body = "Noticing one real moment—even thirst or light—creates a tiny pause that belongs to you; " +
                        "log it when you can.",
                ),
            )
        }
        if (summary.todayLogCount == 0 && summary.streak > 0) {
            tips.add(
                CoachTip(
                    title = "Keep your streak kind",
                    body = "A single line today protects your rhythm without pressure—" +
                        "small consistency is how time-for-you stays real when life is loud.",
                ),
            )
        }
        if (summary.todayLogCount == 0 && summary.daysWithActivityLast7 >= 3 && summary.totalLogsLast7 > 0) {
            tips.add(
                CoachTip(
                    title = "Check in today",
                    body = "You've logged ${summary.daysWithActivityLast7} of the last 7 days—one line about how you feel now keeps the rhythm.",
                ),
            )
        }
        if (summary.totalLogsLast7 >= 10 && summary.todayLogCount > 0) {
            tips.add(
                CoachTip(
                    title = "Rich week",
                    body = "You logged a lot this week (${summary.totalLogsLast7} entries). Notice one pattern you might want more—or less—of next week.",
                ),
            )
        }
        summary.recentNoteExcerpts.firstOrNull()?.let { excerpt ->
            if (tips.size < 2) {
                tips.add(
                    CoachTip(
                        title = "Build on what you noted",
                        body = "You recently logged “${excerpt.take(80)}${if (excerpt.length > 80) "…" else ""}”—choose one small repeat or tweak for today.",
                    ),
                )
            }
        }
        return tips.take(3).ifEmpty {
            listOf(
                CoachTip(
                    title = "Small step",
                    body = "Logging when it feels kind—not when it's polished—trains the app to mirror your real life, " +
                        "not a performance.",
                ),
            )
        }
    }
}
