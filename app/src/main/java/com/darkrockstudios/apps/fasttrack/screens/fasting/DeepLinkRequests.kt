package com.darkrockstudios.apps.fasttrack.screens.fasting

/**
 * Events for deep link or external intent handling to trigger UI actions in Compose screens.
 */

data class StartFastRequest(val startNow: Boolean)

/**
 * Single state object to carry external (deep link/intent) requests down the Compose tree.
 * This helps avoid threading multiple parameters through every composable.
 */
data class ExternalRequests(
	val startFastRequest: StartFastRequest? = null,
	val stopFastRequested: Boolean = false,
	val consumeStartFastRequest: () -> Unit = {},
	val consumeStopFastRequest: () -> Unit = {},
)
