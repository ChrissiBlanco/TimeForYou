package com.timeforyou.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.CoachHeaderMessage
import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachStructuredAdvice
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import com.timeforyou.app.domain.repository.TimeRepository
import com.timeforyou.app.domain.usecase.BuildCoachActivitySummaryUseCase
import com.timeforyou.app.domain.usecase.GetCoachAdviceUseCase
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

data class CoachTip(
    val title: String,
    val body: String,
)

data class CoachUiState(
    val tips: List<CoachTip> = emptyList(),
    val streak: Int = 0,
    val headerMessage: String = "Small steps, gentle pace.",
    val aiAdviceText: String? = null,
    val aiAdviceLoading: Boolean = false,
    val aiAdviceError: String? = null,
    val aiAdviceDisabledReason: String? = null,
)

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val repository: TimeRepository,
    private val buildCoachActivitySummary: BuildCoachActivitySummaryUseCase,
    private val getCoachAdvice: GetCoachAdviceUseCase,
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
            ) { streak, today, logs, days ->
                val summary = buildCoachActivitySummary(
                    streak = streak,
                    todayLogCount = today,
                    logs = logs,
                    lastSevenDays = days,
                )
                val header = CoachHeaderMessage.fromActivity(streak, today)
                Triple(streak, header, summary)
            }
                .debounce(400L)
                .distinctUntilChanged { a, b ->
                    a.third.cacheFingerprint() == b.third.cacheFingerprint()
                }
                .collect { (streak, header, summary) ->
                    latestCoachSummary = summary
                    _uiState.update {
                        it.copy(
                            streak = streak,
                            headerMessage = header,
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
                    state.copy(
                        tips = tipsFromAi.ifEmpty { behaviorFallbackTips(summary) },
                        aiAdviceText = advice.reflection.takeIf { it.isNotBlank() },
                        aiAdviceLoading = false,
                        aiAdviceError = null,
                        aiAdviceDisabledReason = null,
                    )
                }
                result.exceptionOrNull()?.message ==
                    CoachAdviceRepository.MISSING_API_KEY_MESSAGE ->
                    state.copy(
                        tips = behaviorFallbackTips(summary),
                        aiAdviceLoading = false,
                        aiAdviceDisabledReason =
                            "Personalized coach uses OpenAI. Add OPENAI_API_KEY to local.properties " +
                                "and rebuild. Tips below use your recent activity only.",
                        aiAdviceText = null,
                        aiAdviceError = null,
                    )
                else ->
                    state.copy(
                        tips = behaviorFallbackTips(summary),
                        aiAdviceLoading = false,
                        aiAdviceError =
                            result.exceptionOrNull()?.message
                                ?: "Could not load coach insight.",
                        aiAdviceText = null,
                    )
            }
        }
    }

    private fun behaviorFallbackTips(summary: CoachActivitySummary): List<CoachTip> {
        val tips = mutableListOf<CoachTip>()
        if (summary.totalLogsLast7 == 0) {
            tips.add(
                CoachTip(
                    title = "First gentle log",
                    body = "Pick one small moment from today—noticing thirst, light, or a breath—and log it when you can.",
                ),
            )
        }
        if (summary.todayLogCount == 0 && summary.streak > 0) {
            tips.add(
                CoachTip(
                    title = "Keep your streak kind",
                    body = "You're on a ${summary.streak}-day streak; one tiny note today is enough if energy is low.",
                ),
            )
        }
        if (summary.todayLogCount == 0 && summary.daysWithActivityLast7 >= 3 && summary.totalLogsLast7 > 0) {
            tips.add(
                CoachTip(
                    title = "Check in today",
                    body = "You've logged ${summary.daysWithActivityLast7} of the last 7 days—one line about how you feel now keeps the rhythm.",
                ),
            )
        }
        if (summary.totalLogsLast7 >= 10 && summary.todayLogCount > 0) {
            tips.add(
                CoachTip(
                    title = "Rich week",
                    body = "You logged a lot this week (${summary.totalLogsLast7} entries). Notice one pattern you might want more—or less—of next week.",
                ),
            )
        }
        summary.recentNoteExcerpts.firstOrNull()?.let { excerpt ->
            if (tips.size < 2) {
                tips.add(
                    CoachTip(
                        title = "Build on what you noted",
                        body = "You recently logged “${excerpt.take(80)}${if (excerpt.length > 80) "…" else ""}”—choose one small repeat or tweak for today.",
                    ),
                )
            }
        }
        return tips.take(3).ifEmpty {
            listOf(
                CoachTip(
                    title = "Small step",
                    body = "Log one honest moment when it feels kind, not when it's perfect.",
                ),
            )
        }
    }
}
