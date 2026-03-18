package com.yaropaul.mynews.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yaropaul.mynews.ui.screen.detail.DetailScreen
import com.yaropaul.mynews.ui.screen.main.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN
    ) {
        composable(NavRoutes.MAIN) {
            MainScreen(
                onNavigateToDetail = { articleUrl ->
                    navController.navigate(NavRoutes.detailRoute(articleUrl))
                }
            )
        }
        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(
                navArgument(NavRoutes.DETAIL_ARG) { type = NavType.StringType }
            )
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
