package com.darkrockstudios.apps.fasttrack.screens.intro

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test that steps through the intro slides.
 */
@RunWith(AndroidJUnit4::class)
class IntroComposeTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val context = InstrumentationRegistry.getInstrumentation().targetContext

	@Test
	fun testIntroSlides() {
		// Set up the compose content
		composeTestRule.setContent {
			FastTrackTheme {
				IntroScreen(onComplete = {}, onNotificationSlideExited = {})
			}
		}

		// Verify the first slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_00_title)).assertExists()

		// Click the Next button to go to the second slide
		composeTestRule.onNodeWithText(context.getString(R.string.next_button)).performClick()

		// Verify the second slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_01_title)).assertExists()

		// Click the Next button to go to the third slide
		composeTestRule.onNodeWithText(context.getString(R.string.next_button)).performClick()

		// Verify the third slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_02_title)).assertExists()

		// Click the Next button to go to the fourth slide
		composeTestRule.onNodeWithText(context.getString(R.string.next_button)).performClick()

		// Verify the fourth slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_03_title)).assertExists()

		// Click the Next button to go to the fifth slide
		composeTestRule.onNodeWithText(context.getString(R.string.next_button)).performClick()

		// Verify the fifth slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_04_title)).assertExists()

		// Click the Next button to go to the fifth slide
		composeTestRule.onNodeWithText(context.getString(R.string.next_button)).performClick()

		// Verify the sixth slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_05_title)).assertExists()

		// Click the Done button to complete the intro
		composeTestRule.onNodeWithText(context.getString(R.string.done_button)).performClick()
	}

	@Test
	fun testSkipIntro() {
		// Set up the compose content
		composeTestRule.setContent {
			FastTrackTheme {
				IntroScreen(onComplete = {}, onNotificationSlideExited = {})
			}
		}

		// Verify the first slide is displayed
		composeTestRule.onNodeWithText(context.getString(R.string.intro_00_title)).assertExists()

		// Click the Skip button to skip the intro
		composeTestRule.onNodeWithText(context.getString(R.string.skip_button)).performClick()
	}
}
