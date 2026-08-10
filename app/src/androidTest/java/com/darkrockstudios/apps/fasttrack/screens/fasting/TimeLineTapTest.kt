package com.darkrockstudios.apps.fasttrack.screens.fasting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.darkrockstudios.apps.fasttrack.data.Phase
import com.darkrockstudios.apps.fasttrack.data.Stages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Taps land on the phase they visually cover. Expected coordinates are written out longhand rather
 * than reusing the production geometry, so a change to the bar layout has to be made deliberately.
 */
@RunWith(AndroidJUnit4::class)
class TimeLineTapTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private fun phaseCenterX(index: Int) = START_OFFSET + index * PHASE_STRIDE + PHASE_WIDTH / 2f

	private fun content(clicked: MutableList<Phase>) {
		// The blink animation never settles, so the clock has to be driven by hand.
		composeTestRule.mainClock.autoAdvance = false
		composeTestRule.setContent {
			CompositionLocalProvider(LocalDensity provides Density(density = 2f, fontScale = 1f)) {
				Box(modifier = Modifier.size(360.dp, 64.dp)) {
					TimeLine(
						elapsedHours = 30.0,
						modifier = Modifier.testTag(TAG),
						onPhaseClick = { clicked += it }
					)
				}
			}
		}
	}

	private fun tap(x: Float, y: Float) {
		composeTestRule.onNodeWithTag(TAG).performTouchInput { click(Offset(x, y)) }
	}

	@Test
	fun tappingEachPhaseCenterReportsThatPhase() {
		val clicked = mutableListOf<Phase>()
		content(clicked)

		Stages.phases.indices.forEach { index -> tap(phaseCenterX(index), CENTER_Y) }

		assertEquals(Stages.phases.toList(), clicked)
	}

	@Test
	fun tappingAboveOrBelowTheBarIsIgnored() {
		val clicked = mutableListOf<Phase>()
		content(clicked)

		tap(phaseCenterX(2), CENTER_Y - BAR_HALF - 4f)
		tap(phaseCenterX(2), CENTER_Y + BAR_HALF + 4f)

		assertTrue(clicked.toString(), clicked.isEmpty())
	}

	private companion object {
		const val TAG = "timeline"

		// 360 dp wide at density 2 -> 720 px. padding 16 dp = 32 px, spacing 4 dp = 8 px,
		// slant 8 dp = 16 px, bar 16 dp = 32 px.
		// phaseWidth = (720 - 2*32 - 4*8) / 5 = 124.8
		// totalWidth = 5*124.8 + 4*8 + 16 = 672, so startOffset = (720 - 672) / 2 = 24
		const val PHASE_WIDTH = 124.8f
		const val PHASE_STRIDE = PHASE_WIDTH + 8f
		const val START_OFFSET = 24f
		const val CENTER_Y = 32f
		const val BAR_HALF = 16f
	}
}
