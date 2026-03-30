package com.timeforyou.app.domain.model

data class BehaviorLog(
    val id: Long,
    val timestampEpochMillis: Long,
    val category: String?,
    val note: String?,
    val completed: Boolean,
    val score: Int?,
)
