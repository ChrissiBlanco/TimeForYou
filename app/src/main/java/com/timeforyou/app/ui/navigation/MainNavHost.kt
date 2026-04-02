package com.timeforyou.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    contentPadding: PaddingValues,
) {
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
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = vm)
        }
        composable(NavRoutes.Insights.route) {
            val vm: InsightsViewModel = hiltViewModel()
            InsightsScreen(viewModel = vm)
        }
        composable(NavRoutes.Coach.route) {
            val vm: CoachViewModel = hiltViewModel()
            CoachScreen(viewModel = vm)
        }
        composable(NavRoutes.Profile.route) {
            val vm: ProfileViewModel = hiltViewModel()
            ProfileScreen(viewModel = vm)
        }
    }
}
