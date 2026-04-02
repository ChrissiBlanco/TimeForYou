package com.timeforyou.app.data.mapper

import com.timeforyou.app.data.local.BehaviorLogEntity
import com.timeforyou.app.domain.model.BehaviorLog

internal fun BehaviorLogEntity.toDomain(): BehaviorLog =
    BehaviorLog(
        id = id,
        timestampEpochMillis = timestampEpochMillis,
        category = category,
        note = note,
        completed = completed,
        score = score,
    )
