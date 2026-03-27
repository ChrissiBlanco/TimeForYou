package com.timeforyou.app.data.repository

import com.timeforyou.app.data.local.BehaviorLogEntity
import kotlinx.coroutines.flow.Flow

data class DayAggregate(
    val dayStartEpochMillis: Long,
    val logCount: Int,
)

interface TimeRepository {
    fun observeLogs(): Flow<List<BehaviorLogEntity>>
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
