package com.timeforyou.app.domain.repository

import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachStructuredAdvice

interface CoachAdviceRepository {
    suspend fun getAdvice(summary: CoachActivitySummary): Result<CoachStructuredAdvice>

    companion object {
        const val MISSING_API_KEY_MESSAGE = "coach_advice_missing_api_key"
    }
}
