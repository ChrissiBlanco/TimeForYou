package com.timeforyou.app.data.repository

import com.timeforyou.app.data.local.BehaviorLogDao
import com.timeforyou.app.data.local.BehaviorLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TimeRepositoryImpl(
    private val dao: BehaviorLogDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : TimeRepository {

    override fun observeLogs(): Flow<List<BehaviorLogEntity>> = dao.observeAll()

    override fun observeStreak(): Flow<Int> =
        dao.observeAll().map { logs -> computeStreak(logs, zoneId) }

    override fun observeTodayLogCount(): Flow<Int> =
        dao.observeAll().map { logs ->
            val today = LocalDate.now(zoneId)
            logs.count { log ->
                Instant.ofEpochMilli(log.timestampEpochMillis).atZone(zoneId).toLocalDate() == today
            }
        }

    override fun observeLastSevenDays(): Flow<List<DayAggregate>> =
        dao.observeAll().map { logs -> aggregateLastDays(logs, zoneId, 7) }

    override fun observeWeekCompletionFraction(): Flow<Float> =
        observeLastSevenDays().map { days ->
            if (days.isEmpty()) 0f
            else (days.count { it.logCount > 0 } / 7f).coerceIn(0f, 1f)
        }

    override suspend fun logMoment(category: String?, note: String?, timestampEpochMillis: Long?) {
        dao.insert(
            BehaviorLogEntity(
                timestampEpochMillis = timestampEpochMillis ?: System.currentTimeMillis(),
                category = category,
                note = note,
                completed = true,
                score = null,
            ),
        )
    }

    override suspend fun clearAllData() {
        dao.clearAll()
    }

    private companion object {
        fun computeStreak(logs: List<BehaviorLogEntity>, zone: ZoneId): Int {
            if (logs.isEmpty()) return 0
            val daysWithLogs = logs
                .map { Instant.ofEpochMilli(it.timestampEpochMillis).atZone(zone).toLocalDate() }
                .toSet()
            var current = LocalDate.now(zone)
            if (current !in daysWithLogs) {
                current = current.minusDays(1)
                if (current !in daysWithLogs) return 0
            }
            var streak = 0
            while (current in daysWithLogs) {
                streak++
                current = current.minusDays(1)
            }
            return streak
        }

        fun aggregateLastDays(
            logs: List<BehaviorLogEntity>,
            zone: ZoneId,
            dayCount: Int,
        ): List<DayAggregate> {
            val today = LocalDate.now(zone)
            val start = today.minusDays((dayCount - 1).toLong())
            val counts = mutableMapOf<LocalDate, Int>()
            for (log in logs) {
                val d = Instant.ofEpochMilli(log.timestampEpochMillis).atZone(zone).toLocalDate()
                if (!d.isBefore(start) && !d.isAfter(today)) {
                    counts[d] = (counts[d] ?: 0) + 1
                }
            }
            return (0 until dayCount).map { offset ->
                val day = start.plusDays(offset.toLong())
                val startMillis = day.atStartOfDay(zone).toInstant().toEpochMilli()
                DayAggregate(
                    dayStartEpochMillis = startMillis,
                    logCount = counts[day] ?: 0,
                )
            }
        }
    }
}
