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
    suspend fun logMoment(
        category: String? = null,
        note: String? = null,
        timestampEpochMillis: Long? = null,
    )
    suspend fun clearAllData()
}
