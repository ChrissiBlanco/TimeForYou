package com.timeforyou.app.domain.model

data class HomeDashboardSnapshot(
    val todaysMoments: List<BehaviorLog>,
    val momentSuggestions: List<String>,
    val streakAtRisk: Boolean,
    val subtitle: String,
)
