package com.timeforyou.app.domain.model

data class CoachStructuredAdvice(
    /** 1–2 sentences: streak + when they tend to log; snapshot-grounded only. */
    val insightSummary: String,
    val tips: List<CoachAdviceTip>,
)

data class CoachAdviceTip(
    val title: String,
    val body: String,
)
