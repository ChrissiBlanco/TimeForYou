package com.timeforyou.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.timeforyou.app.data.repository.TimeRepository
import com.timeforyou.app.ui.coach.CoachViewModel
import com.timeforyou.app.ui.home.HomeViewModel
import com.timeforyou.app.ui.insights.InsightsViewModel
import com.timeforyou.app.ui.profile.ProfileViewModel

class AppViewModelFactory(
    private val repository: TimeRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(InsightsViewModel::class.java) ->
                InsightsViewModel(repository) as T
            modelClass.isAssignableFrom(CoachViewModel::class.java) ->
                CoachViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
}
