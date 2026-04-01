package com.timeforyou.app.domain.repository

import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.model.DayAggregate
import kotlinx.coroutines.flow.Flow

interface TimeRepository {
    fun observeLogs(): Flow<List<BehaviorLog>>
    fun observeStreak(): Flow<Int>
    fun observeTodayLogCount(): Flow<Int>
    fun observeLastSevenDays(): Flow<List<DayAggregate>>
    fun observeWeekCompletionFraction(): Flow<Float>
    /** Recompute rolling day windows (e.g. last 7 days) using the current local date. */
    fun refreshCalendarWindow()
    suspend fun logMoment(
        category: String? = null,
        note: String? = null,
        timestampEpochMillis: Long? = null,
    )
    suspend fun clearAllData()
}
