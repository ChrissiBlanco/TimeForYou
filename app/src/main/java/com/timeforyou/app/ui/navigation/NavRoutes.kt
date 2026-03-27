package com.timeforyou.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoutes(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : NavRoutes("home", "Home", Icons.Outlined.Home)
    data object Insights : NavRoutes("insights", "Insights", Icons.Outlined.QueryStats)
    data object Coach : NavRoutes("coach", "Coach", Icons.Outlined.Spa)
    data object Profile : NavRoutes("profile", "Profile", Icons.Outlined.Person)

    companion object {
        val bottomBarItems = listOf(Home, Insights, Coach, Profile)
    }
}
