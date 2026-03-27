package com.timeforyou.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.timeforyou.app.AppViewModelFactory
import com.timeforyou.app.data.repository.TimeRepository
import com.timeforyou.app.ui.coach.CoachScreen
import com.timeforyou.app.ui.coach.CoachViewModel
import com.timeforyou.app.ui.home.HomeScreen
import com.timeforyou.app.ui.home.HomeViewModel
import com.timeforyou.app.ui.insights.InsightsScreen
import com.timeforyou.app.ui.insights.InsightsViewModel
import com.timeforyou.app.ui.profile.ProfileScreen
import com.timeforyou.app.ui.profile.ProfileViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    repository: TimeRepository,
    contentPadding: PaddingValues,
) {
    val factory = remember(repository) { AppViewModelFactory(repository) }
    val topPad = contentPadding.calculateTopPadding()
    val bottomPad = contentPadding.calculateBottomPadding()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
        modifier = Modifier
            .padding(top = topPad)
            .padding(bottom = bottomPad),
    ) {
        composable(NavRoutes.Home.route) {
            val vm: HomeViewModel = viewModel(factory = factory)
            HomeScreen(viewModel = vm)
        }
        composable(NavRoutes.Insights.route) {
            val vm: InsightsViewModel = viewModel(factory = factory)
            InsightsScreen(viewModel = vm)
        }
        composable(NavRoutes.Coach.route) {
            val vm: CoachViewModel = viewModel(factory = factory)
            CoachScreen(viewModel = vm)
        }
        composable(NavRoutes.Profile.route) {
            val vm: ProfileViewModel = viewModel(factory = factory)
            ProfileScreen(viewModel = vm)
        }
    }
}
