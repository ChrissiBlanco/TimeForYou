package com.timeforyou.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "behavior_logs")
data class BehaviorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val category: String?,
    val note: String?,
    val completed: Boolean = true,
    val score: Int? = null,
)
