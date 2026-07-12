package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.screens.fasting.ExternalRequests
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingScreen
import com.darkrockstudios.apps.fasttrack.screens.log.LogScreen
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileScreen
import com.darkrockstudios.apps.fasttrack.utils.Utils
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

enum class ScreenPages {
	Fasting,
	Log,
	Profile;

	companion object {
		fun fromOrdinal(ordinal: Int): ScreenPages {
			return when (ordinal) {
				0 -> Fasting
				1 -> Log
				2 -> Profile
				else -> throw IllegalArgumentException("Invalid ordinal")
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
@Composable
fun MainScreen(
	onShareClick: () -> Unit,
	onInfoClick: () -> Unit,
	onAboutClick: () -> Unit,
	onSettingsClick: () -> Unit,
	externalRequests: ExternalRequests = ExternalRequests(),
) {
	val pagerState =
		rememberPagerState(
			initialPage = ScreenPages.Fasting.ordinal,
			pageCount = { ScreenPages.entries.size })
	val coroutineScope = rememberCoroutineScope()

	val fastingTitle = stringResource(id = R.string.title_fasting)
	val logTitle = stringResource(id = R.string.title_log)
	val profileTitle = stringResource(id = R.string.title_profile)

	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val compactHeight = windowSizeClass.minHeightDp < windowSizeClass.minWidthDp

	// No top app bar: the bottom navigation already names the screen, and the
	// reclaimed space belongs to the content. Actions float in the top-right.
	Scaffold(
		bottomBar = {
			if (compactHeight.not()) {
				NavigationBar(
					modifier = Modifier.Companion
						.background(MaterialTheme.colorScheme.primary)
						.fillMaxWidth()
				) {
					NavigationBarItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_fasting),
								contentDescription = fastingTitle,
							)
						},
						label = { Text(fastingTitle) },
						selected = pagerState.currentPage == ScreenPages.Fasting.ordinal,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(0)
							}
						}
					)

					NavigationBarItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_log),
								contentDescription = logTitle
							)
						},
						label = { Text(logTitle) },
						selected = pagerState.currentPage == 1,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(1)
							}
						}
					)

					NavigationBarItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_profile),
								contentDescription = profileTitle
							)
						},
						label = { Text(profileTitle) },
						selected = pagerState.currentPage == 2,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(2)
							}
						}
					)
				}
			}
		}
	) { paddingValues ->
		Box(modifier = Modifier.fillMaxSize()) {
			if (compactHeight) {

				Row(
					modifier = Modifier
						.padding(top = paddingValues.calculateTopPadding())
						.fillMaxSize()
				) {
					NavigationRail {
					NavigationRailItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_fasting),
								contentDescription = fastingTitle,
							)
						},
						label = { Text(fastingTitle) },
						selected = pagerState.currentPage == 0,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(0)
							}
						}
					)

					NavigationRailItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_log),
								contentDescription = logTitle
							)
						},
						label = { Text(logTitle) },
						selected = pagerState.currentPage == 1,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(1)
							}
						}
					)

					NavigationRailItem(
						icon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_profile),
								contentDescription = profileTitle
							)
						},
						label = { Text(profileTitle) },
						selected = pagerState.currentPage == 2,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(2)
							}
						}
					)
				}

				Content(
					Modifier.weight(1f),
					contentPaddingValues = PaddingValues(
						end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
						bottom = paddingValues.calculateBottomPadding(),
					),
					pagerState,
					externalRequests,
				)
			}
			} else {
				Content(
					Modifier.fillMaxSize(),
					contentPaddingValues = paddingValues,
					pagerState,
					externalRequests,
				)
			}

			FloatingTopActions(
				showShare = pagerState.currentPage == ScreenPages.Fasting.ordinal,
				onShareClick = onShareClick,
				onInfoClick = onInfoClick,
				onAboutClick = onAboutClick,
				onSettingsClick = onSettingsClick,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(
						top = paddingValues.calculateTopPadding() + 4.dp,
						end = 8.dp,
					)
			)
		}
	}
}

/**
 * The old top app bar, distilled to a small translucent pill in the
 * top-right corner: Info stays exposed; About and Settings live behind
 * the overflow menu. Share is contextual — it captures the fasting hero,
 * so it only appears (animated) while the Fasting page is active.
 */
@Composable
private fun FloatingTopActions(
	showShare: Boolean,
	onShareClick: () -> Unit,
	onInfoClick: () -> Unit,
	onAboutClick: () -> Unit,
	onSettingsClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var showMenu by remember { mutableStateOf(false) }

	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(24.dp),
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			AnimatedVisibility(
				visible = showShare,
				enter = expandHorizontally() + fadeIn(),
				exit = shrinkHorizontally() + fadeOut(),
			) {
				IconButton(onClick = onShareClick) {
					Icon(
						imageVector = Icons.Default.Share,
						contentDescription = stringResource(id = R.string.action_share),
					)
				}
			}

			IconButton(onClick = onInfoClick) {
				Icon(
					imageVector = Icons.Default.Info,
					contentDescription = stringResource(id = R.string.action_info),
				)
			}

			Box {
				IconButton(onClick = { showMenu = !showMenu }) {
					Icon(
						imageVector = Icons.Default.MoreVert,
						contentDescription = stringResource(id = R.string.more_options_button_description),
					)
				}

				DropdownMenu(
					expanded = showMenu,
					onDismissRequest = { showMenu = false }
				) {
					DropdownMenuItem(
						text = { Text(stringResource(id = R.string.action_about)) },
						leadingIcon = {
							Icon(
								imageVector = Icons.Outlined.Info,
								contentDescription = null,
							)
						},
						onClick = {
							onAboutClick()
							showMenu = false
						},
					)

					DropdownMenuItem(
						text = { Text(stringResource(id = R.string.action_settings)) },
						leadingIcon = {
							Icon(
								imageVector = Icons.Default.Settings,
								contentDescription = null,
							)
						},
						onClick = {
							onSettingsClick()
							showMenu = false
						},
					)
				}
			}
		}
	}
}

@Composable
private fun Content(
	modifier: Modifier,
	contentPaddingValues: PaddingValues,
	pagerState: PagerState,
	externalRequests: ExternalRequests,
) {
	val stateHolder = rememberSaveableStateHolder()
	HorizontalPager(
		modifier = modifier,
		state = pagerState,
		key = { page -> page },
		beyondViewportPageCount = pagerState.pageCount,
	) { page ->
		stateHolder.SaveableStateProvider(key = page) {
			PageContainer(
				page = ScreenPages.fromOrdinal(page),
				contentPaddingValues = contentPaddingValues,
				externalRequests = externalRequests,
			)
		}
	}
}

@ExperimentalTime
@Composable
private fun PageContainer(
	page: ScreenPages,
	contentPaddingValues: PaddingValues,
	externalRequests: ExternalRequests,
) {
	when (page) {
		ScreenPages.Fasting -> {
			FastingScreen(
				contentPaddingValues = contentPaddingValues,
				externalRequests = externalRequests,
			)
		}

		ScreenPages.Log -> {
			LogScreen(contentPaddingValues)
		}

		ScreenPages.Profile -> {
			val context = LocalContext.current
			ProfileScreen(
				contentPaddingValues = contentPaddingValues,
				onShowInfoDialog = { titleRes, contentRes ->
					Utils.showInfoDialog(
						titleRes,
						contentRes,
						context
					)
				}
			)
		}
	}
}
