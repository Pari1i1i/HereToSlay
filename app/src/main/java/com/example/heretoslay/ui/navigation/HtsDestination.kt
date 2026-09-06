package com.example.heretoslay.ui.navigation

/**
 * Navigation destinations for the HTS app.
 *
 * Using a sealed class hierarchy keeps route strings type-safe
 * and centralizes any argument passing.
 */
sealed class HtsDestination(val route: String) {

    /** Animated splash / logo screen shown at launch */
    data object Splash : HtsDestination("splash")

    /** Home screen — entry point for create/join room */
    data object Home : HtsDestination("home")

    /**
     * Lobby screen — shown after creating or joining a room.
     * [roomCode] is passed as a path argument.
     */
    data object Lobby : HtsDestination("lobby/{roomCode}") {
        fun withCode(code: String) = "lobby/$code"
        const val ARG_ROOM_CODE = "roomCode"
    }

    /**
     * Main game board — shown when the host starts the game.
     * [roomCode] is passed as a path argument.
     */
    data object Game : HtsDestination("game/{roomCode}") {
        fun withCode(code: String) = "game/$code"
        const val ARG_ROOM_CODE = "roomCode"
    }

    /** Game over / results screen */
    data object GameOver : HtsDestination("game_over/{roomCode}") {
        fun withCode(code: String) = "game_over/$code"
        const val ARG_ROOM_CODE = "roomCode"
    }
}
