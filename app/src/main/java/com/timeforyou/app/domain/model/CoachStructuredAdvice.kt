package com.timeforyou.app.domain.model

data class CoachStructuredAdvice(
    val reflection: String,
    val tips: List<CoachAdviceTip>,
)

data class CoachAdviceTip(
    val title: String,
    val body: String,
)
