package com.timeforyou.app.domain

/**
 * Coach tab header copy from streak / today activity (presentation copy driven by domain rules).
 */
object CoachHeaderMessage {

    fun fromActivity(streak: Int, todayLogCount: Int): String =
        when {
            streak >= 7 -> "Your streak is glowing—rest is part of the practice too."
            streak >= 3 -> "Consistency is showing up. Keep the rhythm gentle."
            todayLogCount > 0 -> "You’ve tended to yourself today. That matters."
            else -> "Small steps, gentle pace."
        }
}
