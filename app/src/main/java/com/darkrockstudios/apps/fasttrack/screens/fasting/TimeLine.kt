package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkrockstudios.apps.fasttrack.data.FastingJourney
import com.darkrockstudios.apps.fasttrack.data.JourneyStage
import com.darkrockstudios.apps.fasttrack.ui.theme.LocalDarkTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Legacy phase colors, still used by the Log screen.
 */
val gaugeColors = listOf(
	Color.White,
	Color.Green,
	Color.Yellow,
	Color.Red,
	Color.Magenta
)

/** Journey stage colors tuned for dark backgrounds: dawn → fire → clarity → renewal. */
private val journeyColorsDark = listOf(
	Color(0xFFF5F0E1), // Fueling Up: warm ivory
	Color(0xFFF0E4C0), // Settling In: pale sand
	Color(0xFFEDD9A0), // Finding Balance: soft gold
	Color(0xFFE8CD7E), // Inner Alchemy: deep gold
	Color(0xFF7BE495), // The Burn Begins: spring green
	Color(0xFFFFD54F), // Clear Waters: amber
	Color(0xFFFF7A6B), // Deep Renewal: coral
	Color(0xFFF48FB1), // Strength Rising: rose
	Color(0xFFCE8FFF), // Fine Tuning: violet
	Color(0xFFA98BFF), // Rebirth: deep violet
)

/** Journey stage colors tuned for light backgrounds. */
private val journeyColorsLight = listOf(
	Color(0xFFCFA24E), // Fueling Up: warm gold
	Color(0xFFD49A3F), // Settling In: deeper gold
	Color(0xFFD98F2F), // Finding Balance: amber gold
	Color(0xFFDE8420), // Inner Alchemy: amber
	Color(0xFF2E9E5B), // The Burn Begins: deep green
	Color(0xFFE6A817), // Clear Waters: goldenrod
	Color(0xFFDE5B4C), // Deep Renewal: terracotta
	Color(0xFFC2559E), // Strength Rising: magenta rose
	Color(0xFF9450C8), // Fine Tuning: royal violet
	Color(0xFF7A3FB8), // Rebirth: deep violet
)

/** Accent color of a journey stage, matching the dial's gradient. */
@Composable
fun journeyStageColor(stageIndex: Int): Color {
	val colors = if (LocalDarkTheme.current) journeyColorsDark else journeyColorsLight
	return colors.getOrElse(stageIndex) { colors.last() }
}

// The ring is an open arc: 270 degrees of sweep with the gap facing down,
// like a vessel that is being filled.
private const val START_ANGLE = 135f
private const val TOTAL_SWEEP = 270f

// Golden-ratio derived proportions (phi = 1.618...)
private const val PHI = 1.618034f
private const val STROKE_FRACTION = 1f / (PHI * PHI * PHI * PHI) / 2.4f // of min dimension
private const val TRACK_ALPHA_DARK = 0.16f
private const val TRACK_ALPHA_LIGHT = 0.24f

// Beyond this many hours the knob rests at the arc's end; the dial reads
// identically for a 4-day or a 40-day fast (the center timer carries the days).
private val FINAL_STAGE = FastingJourney.stages.last()
private val FINAL_STAGE_VISUAL_END_HOURS = FINAL_STAGE.startHours + 24f

/**
 * Fasting journey dial: a circular mandala-like gauge. The ten journey stages
 * wrap around an open ring as a continuous color gradient with an emoji
 * milestone bubble at each stage's heart; fine radial ticks give it a subtle
 * fractal texture. On first composition the lit arc blooms from zero up to the
 * current position, where a glowing knob breathes gently. Milestones wake up
 * one by one as the arc reaches them.
 *
 * [content] is rendered centered inside the ring (the timer lives there).
 * Tapping a milestone bubble, the knob, or anywhere on the band reports the
 * corresponding [JourneyStage] via [onStageClick].
 */
@Composable
fun TimeLine(
	elapsedHours: Double,
	modifier: Modifier = Modifier,
	onStageClick: (JourneyStage) -> Unit = {},
	content: @Composable () -> Unit = {}
) {
	val isDark = LocalDarkTheme.current
	val ringColors = if (isDark) journeyColorsDark else journeyColorsLight
	val trackAlpha = if (isDark) TRACK_ALPHA_DARK else TRACK_ALPHA_LIGHT
	val outlineColor = MaterialTheme.colorScheme.onBackground
	val bubbleColor = MaterialTheme.colorScheme.surfaceVariant

	val stages = FastingJourney.stages
	val segmentSweep = TOTAL_SWEEP / stages.size
	val curIndex = FastingJourney.indexFor(elapsedHours)
	val targetNeedle = needleSweep(elapsedHours, segmentSweep)

	// Intro bloom: the lit arc sweeps from zero to the current position once,
	// with a long decelerating tail so it settles rather than stops.
	val intro = remember { Animatable(0f) }
	LaunchedEffect(Unit) {
		intro.animateTo(
			targetValue = 1f,
			animationSpec = tween(
				durationMillis = 2100,
				easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
			)
		)
	}

	// Slow breathing pulse for the comet head — only while a fast is running.
	// When idle nobody drives the animation, so the Canvas stops invalidating
	// every frame (no wasted battery on a screen that isn't visibly moving).
	val pulsing = elapsedHours > 0
	val breathAnim = remember { Animatable(0f) }
	LaunchedEffect(pulsing) {
		if (pulsing) {
			breathAnim.animateTo(
				targetValue = 1f,
				animationSpec = infiniteRepeatable(
					animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
					repeatMode = RepeatMode.Reverse
				)
			)
		} else {
			breathAnim.snapTo(0f)
		}
	}
	val breath = breathAnim.value

	// The sweep gradient's color stops depend only on the palette and geometry,
	// not on time — remember them so a fasting redraw doesn't rebuild the array
	// (and Brush) on every animation frame.
	val stageStops = remember(ringColors, segmentSweep) {
		Array(stages.size + 2) { i ->
			when (i) {
				0 -> 0f to ringColors.first()
				stages.size + 1 -> 1f to ringColors.last()
				else -> ((i - 0.5f) * segmentSweep / 360f) to ringColors[i - 1]
			}
		}
	}

	Box(
		modifier = modifier.aspectRatio(1f),
		contentAlignment = Alignment.Center
	) {
		Canvas(
			modifier = Modifier
				.fillMaxSize()
				.pointerInput(Unit) {
					detectTapGestures { offset ->
						val minDim = min(size.width, size.height).toFloat()
						val stroke = minDim * STROKE_FRACTION
						val radius = minDim / 2f - stroke * 1.18f
						val center = Offset(size.width / 2f, size.height / 2f)

						val dx = offset.x - center.x
						val dy = offset.y - center.y
						val dist = sqrt(dx * dx + dy * dy)
						// Only accept taps on the ring band itself. The knob always
						// sits inside the current stage's segment, so tapping it
						// naturally opens the current stage.
						if (dist < radius - stroke * 1.7f || dist > radius + stroke * 1.7f) {
							return@detectTapGestures
						}

						var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
						if (angle < 0f) angle += 360f
						var sweep = angle - START_ANGLE
						if (sweep < 0f) sweep += 360f
						if (sweep > TOTAL_SWEEP) return@detectTapGestures

						val segSweep = TOTAL_SWEEP / FastingJourney.stages.size
						val index = min((sweep / segSweep).toInt(), FastingJourney.stages.size - 1)
						onStageClick(FastingJourney.stages[index])
					}
				}
		) {
			val minDim = min(size.width, size.height)
			val stroke = minDim * STROKE_FRACTION
			val radius = minDim / 2f - stroke * 1.18f
			val center = Offset(size.width / 2f, size.height / 2f)
			val arcSize = Size(radius * 2f, radius * 2f)
			val arcTopLeft = Offset(center.x - radius, center.y - radius)

			val litSweep = (targetNeedle * intro.value).coerceIn(0f, TOTAL_SWEEP)

			// One continuous gradient flowing through the stage colors,
			// anchored at each stage segment's midpoint.
			val phaseBrush = Brush.sweepGradient(colorStops = stageStops, center = center)

			// Rotate so relative angle 0 is the arc start; the sweep gradient
			// rotates with the geometry, keeping its stops aligned to stages.
			rotate(degrees = START_ANGLE, pivot = center) {
				// Fine radial ticks: quiet fractal texture just inside the band,
				// with stronger ticks landing on stage boundaries.
				val tickStep = segmentSweep / 3f
				val tickCount = (TOTAL_SWEEP / tickStep).toInt()
				for (i in 0..tickCount) {
					val angle = Math.toRadians((i * tickStep).toDouble())
					val major = i % 3 == 0
					val rOuter = radius - stroke * 0.95f
					val rInner = rOuter - (if (major) stroke * 0.62f else stroke * 0.34f)
					val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
					drawLine(
						color = outlineColor.copy(alpha = if (major) 0.22f else 0.10f),
						start = center + dir * rInner,
						end = center + dir * rOuter,
						strokeWidth = (if (major) 1.5.dp else 1.dp).toPx(),
						cap = StrokeCap.Round
					)
				}

				// Unlit track: the full journey, faintly visible
				drawArc(
					brush = phaseBrush,
					startAngle = 0f,
					sweepAngle = TOTAL_SWEEP,
					useCenter = false,
					topLeft = arcTopLeft,
					size = arcSize,
					style = Stroke(width = stroke, cap = StrokeCap.Round),
					alpha = trackAlpha
				)

				if (litSweep > 0f) {
					// Soft aura beneath the lit arc, breathing slowly
					drawArc(
						brush = phaseBrush,
						startAngle = 0f,
						sweepAngle = litSweep,
						useCenter = false,
						topLeft = arcTopLeft,
						size = arcSize,
						style = Stroke(width = stroke * (1.9f + 0.35f * breath), cap = StrokeCap.Round),
						alpha = 0.10f + 0.06f * breath
					)

					// The lit arc itself
					drawArc(
						brush = phaseBrush,
						startAngle = 0f,
						sweepAngle = litSweep,
						useCenter = false,
						topLeft = arcTopLeft,
						size = arcSize,
						style = Stroke(width = stroke, cap = StrokeCap.Round)
					)

					// Comet head: the arc's rounded tip IS the current position —
					// no knob, just a breathing glow where the color ends.
					val headRad = Math.toRadians(litSweep.toDouble())
					val headCenter = center +
						Offset(cos(headRad).toFloat(), sin(headRad).toFloat()) * radius
					drawCircle(
						color = ringColors[curIndex].copy(alpha = 0.25f + 0.20f * breath),
						radius = stroke * (1.30f + 0.30f * breath),
						center = headCenter
					)
				}
			}
		}

		// Milestone bubbles, one at the heart of each stage segment.
		// They wake up in sequence as the intro arc blooms past them.
		Layout(
			modifier = Modifier.fillMaxSize(),
			content = {
				stages.forEachIndexed { index, stage ->
					MilestoneBubble(
						emoji = stage.emoji,
						accent = ringColors[index],
						background = bubbleColor,
						// Nothing is "reached" when no fast is running: the dial rests muted
						reached = elapsedHours > 0 && elapsedHours >= stage.startHours,
						isCurrent = index == curIndex && elapsedHours > 0,
						wakeDelayMillis = 250 + index * 140,
						onClick = { onStageClick(stage) }
					)
				}
			}
		) { measurables, constraints ->
			val width = constraints.maxWidth
			val height = constraints.maxHeight
			val minDim = min(width, height).toFloat()
			val stroke = minDim * STROKE_FRACTION
			val radius = minDim / 2f - stroke * 1.18f
			val bubbleSize = (stroke * 2.1f).roundToInt()

			val placeables = measurables.map { it.measure(Constraints.fixed(bubbleSize, bubbleSize)) }

			layout(width, height) {
				placeables.forEachIndexed { index, placeable ->
					val angle = Math.toRadians(
						(START_ANGLE + (index + 0.5f) * segmentSweep).toDouble()
					)
					val x = width / 2f + radius * cos(angle).toFloat() - bubbleSize / 2f
					val y = height / 2f + radius * sin(angle).toFloat() - bubbleSize / 2f
					// place (not placeRelative): the canvas dial never mirrors in RTL
					placeable.place(x.roundToInt(), y.roundToInt())
				}
			}
		}

		// Center content lives inside the ring's inscribed square, so the
		// timer can never spill over the arc. The ring's inner radius is
		// (0.5 - STROKE_FRACTION*1.18); an inscribed square spans that
		// diameter / sqrt(2). A small safety margin keeps text off the band.
		Box(
			modifier = Modifier.fillMaxSize(fraction = INNER_CONTENT_FRACTION),
			contentAlignment = Alignment.Center
		) {
			content()
		}
	}
}

// Fraction of the dial width available to centered content (timer + label).
// Derived from the inscribed square of the inner circle, trimmed for margin.
private val INNER_CONTENT_FRACTION =
	((0.5f - STROKE_FRACTION * 1.18f) * 2f / 1.41421f) * 0.94f

@Composable
private fun MilestoneBubble(
	emoji: String,
	accent: Color,
	background: Color,
	reached: Boolean,
	isCurrent: Boolean,
	wakeDelayMillis: Int,
	onClick: () -> Unit,
) {
	val alpha by animateFloatAsState(
		targetValue = if (reached) 1f else 0.40f,
		animationSpec = tween(durationMillis = 700, delayMillis = wakeDelayMillis),
		label = "bubble_wake"
	)
	// Visual hierarchy: the milestones ahead recede, the current one leads
	val scale by animateFloatAsState(
		targetValue = when {
			isCurrent -> 1.15f
			reached -> 1f
			else -> 0.78f
		},
		animationSpec = tween(durationMillis = 700, delayMillis = wakeDelayMillis),
		label = "bubble_scale"
	)

	BoxWithConstraints(
		modifier = Modifier
			.graphicsLayer {
				this.alpha = alpha
				scaleX = scale
				scaleY = scale
			}
			.clip(CircleShape)
			.background(background)
			.border(1.5.dp, accent.copy(alpha = if (reached) 0.9f else 0.4f), CircleShape)
			.clickable(onClick = onClick),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = emoji,
			fontSize = (maxWidth.value * 0.46f).sp,
		)
	}
}

/**
 * Sweep (in degrees from the arc start) of the current position. Each journey
 * stage occupies an equal segment and the knob interpolates within the current
 * stage based on time into it. Past the visual end of the final stage the knob
 * rests at the arc's end, so a 5-, 10-, or 40-day fast all read the same:
 * a fully lit ring with the knob breathing at the finish.
 */
private fun needleSweep(elapsedHours: Double, segmentSweep: Float): Float {
	if (elapsedHours <= 0) return 0f

	val index = FastingJourney.indexFor(elapsedHours)
	val stage = FastingJourney.stages[index]
	val endHours = stage.endHours?.toFloat() ?: FINAL_STAGE_VISUAL_END_HOURS
	val stageLength = endHours - stage.startHours
	val timeIntoStage = elapsedHours - stage.startHours
	val percent = min(timeIntoStage / stageLength, 1.0).toFloat()

	return (index + percent) * segmentSweep
}
