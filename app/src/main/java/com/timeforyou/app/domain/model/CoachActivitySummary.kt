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
    /** Derived from recent log timestamps (last 7 days), for coach copy only. */
    val typicalLogTimeDescription: String,
    /** Display name from profile (may be placeholder "You"). */
    val displayName: String,
) {
    /** Stable key for skipping redundant coach API calls when activity unchanged. */
    fun cacheFingerprint(): String =
        "$streak:$todayLogCount:$daysWithActivityLast7:$totalLogsLast7:" +
            recentNoteExcerpts.joinToString("\u0001") + "\u0001" + typicalLogTimeDescription +
            "\u0001$displayName"
}
