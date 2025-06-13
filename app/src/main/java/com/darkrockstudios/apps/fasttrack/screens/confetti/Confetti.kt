package com.darkrockstudios.apps.fasttrack.screens.confetti

import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiConfig(
	val particleCount: Int = 200
)

/**
 * State object for controlling confetti effects.
 */
class ConfettiState(
	val config: ConfettiConfig = ConfettiConfig(),
) {
	private val _isPlaying = mutableStateOf(false)
	val isPlaying: State<Boolean> = _isPlaying

	private var animationJob: Job? = null

	/**
	 * Start playing the confetti effect.
	 */
	fun start(scope: CoroutineScope) {
		stop()
		_isPlaying.value = true
	}

	/**
	 * Stop the confetti effect prematurely.
	 */
	fun stop() {
		animationJob?.cancel()
		animationJob = null
		_isPlaying.value = false
	}
}

/**
 * Particle type configuration for drawing
 */
private data class ParticleType(
	val color: Color,
	val size: Dp
)

/**
 * Individual particle with movement properties
 */
private data class Particle(
	val type: ParticleType,
	val startX: Float,
	val velocityX: Float,
	val velocityY: Float,
	var position: Offset
)

/**
 * A modifier that adds a confetti effect to the composable it's applied to.
 */
fun Modifier.confettiEffect(
	state: ConfettiState
): Modifier = composed {
	val particleGroups = remember { mutableStateMapOf<ParticleType, MutableList<Offset>>() }
	val allParticles = remember { mutableStateListOf<Particle>() }

	var width by remember { mutableStateOf(0.dp) }
	var height by remember { mutableStateOf(0.dp) }
	val density = LocalDensity.current

	val isPlaying by state.isPlaying

	val particleTypes = remember {
		listOf(
			ParticleType(Color.Red, 6.dp),
			ParticleType(Color.Blue, 8.dp),
			ParticleType(Color.Green, 5.dp),
			ParticleType(Color.Yellow, 7.dp),
			ParticleType(Color.Magenta, 6.dp)
		)
	}

	// Initialize particles when animation starts
	LaunchedEffect(isPlaying) {
		if (isPlaying) {
			particleGroups.clear()
			allParticles.clear()

			// Initialize particle groups
			particleTypes.forEach { type ->
				particleGroups[type] = mutableStateListOf()
			}

			repeat(state.config.particleCount) {
				// Pick random particle type
				val particleType = particleTypes.random()

				// Create particle
				val particle = Particle(
					type = particleType,
					startX = Random.nextFloat(),
					velocityX = Random.nextFloat() * 2f - 1f,
					velocityY = Random.nextFloat() * 6f + 3f,
					position = with(density) {
						val startY = -height.toPx() * 0.1f
						val startXPos = Random.nextFloat() * width.toPx()
						Offset(startXPos, startY)
					}
				)

				allParticles.add(particle)
				particleGroups[particleType]?.add(particle.position)
			}
		} else {
			particleGroups.clear()
			allParticles.clear()
		}
	}

	val windowManager = LocalContext.current.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	// Calculate frame delay based on refresh rate
	val frameDelay = remember {
		val refreshRate = windowManager.defaultDisplay.refreshRate
		(1000f / refreshRate).toLong().coerceAtLeast(8)
	}

	// Main animation loop
	LaunchedEffect(isPlaying) {
		if (isPlaying) {
			var lastFrameTime = System.currentTimeMillis()

			while (allParticles.isNotEmpty()) {
				val currentTime = System.currentTimeMillis()
				val deltaTime = currentTime - lastFrameTime
				lastFrameTime = currentTime

				// Clear all position lists
				particleGroups.values.forEach { it.clear() }

				// Update particles and rebuild position lists
				val particlesToRemove = mutableListOf<Particle>()

				allParticles.forEach { particle ->
					// Update position
					val newX = particle.position.x + particle.velocityX * deltaTime / 16f
					val newY = particle.position.y + particle.velocityY * deltaTime / 16f
					particle.position = Offset(newX, newY)

					// Check if particle has fallen off screen
					if (newY > with(density) { height.toPx() + 100 }) {
						particlesToRemove.add(particle)
					} else {
						// Add to appropriate group for drawing
						particleGroups[particle.type]?.add(particle.position)
					}
				}

				// Remove particles that have fallen off screen
				allParticles.removeAll(particlesToRemove)

				// Stop animation when all particles are gone
				if (allParticles.isEmpty()) {
					state.stop()
				}

				delay(frameDelay)
			}
		}
	}

	this
		.onGloballyPositioned { coordinates ->
			with(density) {
				width = coordinates.size.width.toDp()
				height = coordinates.size.height.toDp()
			}
		}
		.drawBehind {
			// Draw each particle type in batches
			particleGroups.forEach { (particleType, positions) ->
				if (positions.isNotEmpty()) {
					drawPoints(
						points = positions,
						pointMode = PointMode.Points,
						color = particleType.color,
						strokeWidth = with(density) { particleType.size.toPx() },
						cap = StrokeCap.Round
					)
				}
			}
		}
}