package com.timeforyou.app.data.repository

import com.timeforyou.app.data.local.BehaviorLogDao
import com.timeforyou.app.data.local.BehaviorLogEntity
import com.timeforyou.app.data.mapper.toDomain
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.domain.repository.TimeRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class TimeRepositoryImpl(
    private val dao: BehaviorLogDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : TimeRepository {

    private val calendarWindowBump = MutableStateFlow(0)

    override fun observeLogs(): Flow<List<BehaviorLog>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeStreak(): Flow<Int> =
        combine(
            dao.observeAll(),
            localDayTicker(zoneId),
            calendarWindowBump,
        ) { logs, _, _ ->
            computeStreak(logs, zoneId)
        }

    override fun observeTodayLogCount(): Flow<Int> =
        combine(
            dao.observeAll(),
            localDayTicker(zoneId),
            calendarWindowBump,
        ) { logs, _, _ ->
            val today = LocalDate.now(zoneId)
            logs.count { log ->
                Instant.ofEpochMilli(log.timestampEpochMillis).atZone(zoneId).toLocalDate() == today
            }
        }

    override fun observeLastSevenDays(): Flow<List<DayAggregate>> =
        combine(
            dao.observeAll(),
            localDayTicker(zoneId),
            calendarWindowBump,
        ) { logs, _, _ ->
            aggregateLastDays(logs, zoneId, 7)
        }

    override fun refreshCalendarWindow() {
        calendarWindowBump.update { it + 1 }
    }

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
        /**
         * Emits once on collect (so rolling windows use today immediately), then after each local midnight.
         * Keeps last-N-day aggregates correct without a new database write when the calendar day changes.
         */
        fun localDayTicker(zone: ZoneId): Flow<Unit> =
            flow {
                while (true) {
                    val now = Instant.now()
                    val nextMidnight = now.atZone(zone)
                        .toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
                    val millis = Duration.between(now, nextMidnight).toMillis()
                    if (millis > 0) {
                        delay(millis)
                    }
                    emit(Unit)
                }
            }.onStart { emit(Unit) }

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
