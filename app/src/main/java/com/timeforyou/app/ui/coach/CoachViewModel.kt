package com.timeforyou.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachStructuredAdvice
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import com.timeforyou.app.data.profile.ProfilePreferences
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
    val displayName: String = ProfilePreferences.DEFAULT_NAME,
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
                    val insight = advice.insightSummary.takeIf { it.isNotBlank() }
                        ?: behaviorInsightSummary(summary)
                    state.copy(
                        tips = tipsFromAi.ifEmpty { behaviorFallbackTips(summary) },
                        insightSummary = insight,
                        aiAdviceLoading = false,
                        aiAdviceError = null,
                        aiAdviceDisabledReason = null,
                    )
                }
                result.exceptionOrNull()?.message ==
                    CoachAdviceRepository.MISSING_API_KEY_MESSAGE ->
                    state.copy(
                        tips = behaviorFallbackTips(summary),
                        insightSummary = behaviorInsightSummary(summary),
                        aiAdviceLoading = false,
                        aiAdviceDisabledReason =
                            "Add OPENAI_API_KEY in local.properties for richer coach wording. " +
                                "Summary and suggestions below use your activity only.",
                        aiAdviceError = null,
                    )
                else ->
                    state.copy(
                        tips = behaviorFallbackTips(summary),
                        insightSummary = behaviorInsightSummary(summary),
                        aiAdviceLoading = false,
                        aiAdviceError =
                            result.exceptionOrNull()?.message
                                ?: "Could not load coach insight.",
                    )
            }
        }
    }

    private fun behaviorInsightSummary(summary: CoachActivitySummary): String {
        val rawName = summary.displayName.trim()
        val useName = rawName.isNotEmpty() && !rawName.equals("You", ignoreCase = true)
        val streakPart =
            when {
                summary.streak <= 0 ->
                    if (useName) {
                        "$rawName, you're starting fresh—any single log is a win."
                    } else {
                        "You're starting fresh—any single log is a win."
                    }
                summary.streak == 1 ->
                    if (useName) {
                        "$rawName, you've logged at least one day in a row."
                    } else {
                        "You've logged at least one day in a row."
                    }
                else ->
                    if (useName) {
                        "$rawName, you're on a ${summary.streak}-day logging streak."
                    } else {
                        "You're on a ${summary.streak}-day logging streak."
                    }
            }
        return "$streakPart ${summary.typicalLogTimeDescription}"
    }

    private fun behaviorFallbackTips(summary: CoachActivitySummary): List<CoachTip> {
        val tips = mutableListOf<CoachTip>()
        if (summary.totalLogsLast7 == 0) {
            tips.add(
                CoachTip(
                    title = "First gentle log",
                    body = "Noticing one real moment—even thirst or light—creates a tiny pause that belongs to you; " +
                        "log it when you can.",
                ),
            )
        }
        if (summary.todayLogCount == 0 && summary.streak > 0) {
            tips.add(
                CoachTip(
                    title = "Keep your streak kind",
                    body = "A single line today protects your rhythm without pressure—" +
                        "small consistency is how time-for-you stays real when life is loud.",
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
                    body = "Logging when it feels kind—not when it's polished—trains the app to mirror your real life, " +
                        "not a performance.",
                ),
            )
        }
    }
}
