package com.fachri.heretoslay.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fachri.heretoslay.ui.screen.HomeScreen
import com.fachri.heretoslay.ui.screen.PlaceholderScreen
import com.fachri.heretoslay.ui.screen.SplashScreen

private const val TRANSITION_MS = 350

@Composable
fun HtsNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HtsDestination.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_MS)) +
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_MS),
                    initialOffsetX = { it / 6 },
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_MS)) +
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_MS),
                    targetOffsetX = { -it / 6 },
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_MS)) +
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_MS),
                    initialOffsetX = { -it / 6 },
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_MS)) +
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_MS),
                    targetOffsetX = { it / 6 },
                )
        },
    ) {
        // ── Splash ────────────────────────────────────────────────────────
        composable(route = HtsDestination.Splash.route) {
            SplashScreen(
                onAnimationFinished = {
                    navController.navigate(HtsDestination.Home.route) {
                        popUpTo(HtsDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(route = HtsDestination.Home.route) {
            HomeScreen(
                onNavigateToLobby = { roomCode ->
                    navController.navigate(HtsDestination.Lobby.withCode(roomCode))
                }
            )
        }

        // ── Lobby ─────────────────────────────────────────────────────────
        composable(
            route = HtsDestination.Lobby.route,
            arguments = listOf(
                navArgument(HtsDestination.Lobby.ARG_ROOM_CODE) {
                    type = NavType.StringType
                }
            ),
        ) { backStack ->
            val roomCode = backStack.arguments
                ?.getString(HtsDestination.Lobby.ARG_ROOM_CODE) ?: ""
            // Lobby screen will be implemented in Part 2
            PlaceholderScreen(
                label = "Lobby — Room: $roomCode",
                onBack = { navController.popBackStack() },
            )
        }

        // ── Game ──────────────────────────────────────────────────────────
        composable(
            route = HtsDestination.Game.route,
            arguments = listOf(
                navArgument(HtsDestination.Game.ARG_ROOM_CODE) {
                    type = NavType.StringType
                }
            ),
        ) { backStack ->
            val roomCode = backStack.arguments
                ?.getString(HtsDestination.Game.ARG_ROOM_CODE) ?: ""
            // Game screen will be implemented in Part 3
            PlaceholderScreen(
                label = "Game Board — Room: $roomCode",
                onBack = { navController.popBackStack() },
            )
        }

        // ── Game Over ─────────────────────────────────────────────────────
        composable(
            route = HtsDestination.GameOver.route,
            arguments = listOf(
                navArgument(HtsDestination.GameOver.ARG_ROOM_CODE) {
                    type = NavType.StringType
                }
            ),
        ) { backStack ->
            val roomCode = backStack.arguments
                ?.getString(HtsDestination.GameOver.ARG_ROOM_CODE) ?: ""
            // Game Over screen will be implemented in Part 6
            PlaceholderScreen(
                label = "Game Over — Room: $roomCode",
                onBack = {
                    navController.navigate(HtsDestination.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
