package com.timeforyou.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.domain.repository.TimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val lastSevenDays: List<DayAggregate> = emptyList(),
    val weekCompletionFraction: Float = 0f,
    val totalLogsThisWeek: Int = 0,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TimeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeLastSevenDays().collect { days ->
                val fraction =
                    if (days.isEmpty()) {
                        0f
                    } else {
                        (days.count { it.logCount > 0 } / 7f).coerceIn(0f, 1f)
                    }
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

    fun onResume() {
        repository.refreshCalendarWindow()
    }
}
