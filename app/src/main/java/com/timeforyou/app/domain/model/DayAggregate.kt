package com.timeforyou.app.domain.model

data class DayAggregate(
    val dayStartEpochMillis: Long,
    val logCount: Int,
)
