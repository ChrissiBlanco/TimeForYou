package com.timeforyou.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.domain.repository.TimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val lastSevenDays: List<DayAggregate> = emptyList(),
    val weekCompletionFraction: Float = 0f,
    val totalLogsThisWeek: Int = 0,
)

class InsightsViewModel(
    private val repository: TimeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeLastSevenDays(),
                repository.observeWeekCompletionFraction(),
            ) { days, fraction ->
                Pair(days, fraction)
            }.collect { (days, fraction) ->
                _uiState.update {
                    InsightsUiState(
                        lastSevenDays = days,
                        weekCompletionFraction = fraction,
                        totalLogsThisWeek = days.sumOf { it.logCount },
                    )
                }
            }
        }
    }
}
