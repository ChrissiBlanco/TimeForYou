package com.timeforyou.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.data.profile.ProfilePreferences
import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachStructuredAdvice
import com.timeforyou.app.domain.model.CoachTip
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import com.timeforyou.app.domain.repository.TimeRepository
import com.timeforyou.app.domain.usecase.coach.BuildCoachActivitySummaryUseCase
import com.timeforyou.app.domain.usecase.coach.BuildCoachLocalFallbackUseCase
import com.timeforyou.app.domain.usecase.coach.GetCoachAdviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoachUiState(
    val tips: List<CoachTip> = emptyList(),
    val streak: Int = 0,
    val displayName: String = "",
    /** 1–2 sentences: streak + typical log time; from AI or local fallback. */
    val insightSummary: String? = null,
    val aiAdviceLoading: Boolean = false,
    val aiAdviceError: String? = null,
    val aiAdviceDisabledReason: String? = null,
)

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val repository: TimeRepository,
    private val profilePreferences: ProfilePreferences,
    private val buildCoachActivitySummary: BuildCoachActivitySummaryUseCase,
    private val getCoachAdvice: GetCoachAdviceUseCase,
    private val buildCoachLocalFallback: BuildCoachLocalFallbackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    private var latestCoachSummary: CoachActivitySummary? = null

    init {
        viewModelScope.launch {
            combine(
                repository.observeStreak(),
                repository.observeTodayLogCount(),
                repository.observeLogs(),
                repository.observeLastSevenDays(),
                profilePreferences.displayName,
            ) { streak, today, logs, days, displayName ->
                val summary = buildCoachActivitySummary(
                    streak = streak,
                    todayLogCount = today,
                    logs = logs,
                    lastSevenDays = days,
                    displayName = displayName,
                )
                Pair(streak, summary)
            }
                .debounce(400L)
                .distinctUntilChanged { a, b ->
                    a.second.cacheFingerprint() == b.second.cacheFingerprint()
                }
                .collect { (streak, summary) ->
                    latestCoachSummary = summary
                    _uiState.update {
                        it.copy(
                            streak = streak,
                            displayName = summary.displayName,
                            aiAdviceLoading = true,
                            aiAdviceError = null,
                        )
                    }
                    applyAdviceResult(summary, getCoachAdvice(summary))
                }
        }
    }

    fun retryCoachAdvice() {
        val summary = latestCoachSummary ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(aiAdviceLoading = true, aiAdviceError = null)
            }
            applyAdviceResult(summary, getCoachAdvice(summary))
        }
    }

    private fun applyAdviceResult(summary: CoachActivitySummary, result: Result<CoachStructuredAdvice>) {
        _uiState.update { state ->
            when {
                result.isSuccess -> {
                    val advice = result.getOrThrow()
                    val tipsFromAi = advice.tips.map { CoachTip(title = it.title, body = it.body) }
                    val localInsight = buildCoachLocalFallback.insightSummary(summary)
                    val insight = advice.insightSummary.takeIf { it.isNotBlank() } ?: localInsight
                    state.copy(
                        tips = tipsFromAi.ifEmpty { buildCoachLocalFallback.tips(summary) },
                        insightSummary = insight,
                        aiAdviceLoading = false,
                        aiAdviceError = null,
                        aiAdviceDisabledReason = null,
                    )
                }
                result.exceptionOrNull()?.message ==
                    CoachAdviceRepository.MISSING_API_KEY_MESSAGE ->
                    state.copy(
                        tips = buildCoachLocalFallback.tips(summary),
                        insightSummary = buildCoachLocalFallback.insightSummary(summary),
                        aiAdviceLoading = false,
                        aiAdviceDisabledReason =
                            "Add OPENAI_API_KEY in local.properties for richer coach wording. " +
                                "Summary and suggestions below use your activity only.",
                        aiAdviceError = null,
                    )
                else ->
                    state.copy(
                        tips = buildCoachLocalFallback.tips(summary),
                        insightSummary = buildCoachLocalFallback.insightSummary(summary),
                        aiAdviceLoading = false,
                        aiAdviceError =
                            result.exceptionOrNull()?.message
                                ?: "Could not load coach insight.",
                    )
            }
        }
    }
}
