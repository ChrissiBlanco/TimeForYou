package com.timeforyou.app.domain.model

/**
 * Snapshot of recent logging activity sent to the coach LLM (short excerpts only).
 */
data class CoachActivitySummary(
    val streak: Int,
    val todayLogCount: Int,
    val daysWithActivityLast7: Int,
    val totalLogsLast7: Int,
    /** Short note snippets, each truncated for privacy/size. */
    val recentNoteExcerpts: List<String>,
) {
    /** Stable key for skipping redundant coach API calls when activity unchanged. */
    fun cacheFingerprint(): String =
        "$streak:$todayLogCount:$daysWithActivityLast7:$totalLogsLast7:" +
            recentNoteExcerpts.joinToString("\u0001")
}
