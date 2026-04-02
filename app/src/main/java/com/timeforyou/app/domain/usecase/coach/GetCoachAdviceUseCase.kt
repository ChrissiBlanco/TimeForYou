package com.timeforyou.app.domain.usecase.coach

import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachStructuredAdvice
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import javax.inject.Inject

class GetCoachAdviceUseCase @Inject constructor(
    private val coachAdviceRepository: CoachAdviceRepository,
) {
    suspend operator fun invoke(summary: CoachActivitySummary): Result<CoachStructuredAdvice> =
        coachAdviceRepository.getAdvice(summary)
}
