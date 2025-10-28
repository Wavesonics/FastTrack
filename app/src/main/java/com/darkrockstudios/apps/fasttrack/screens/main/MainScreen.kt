package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepository
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
	repository: ActiveFastRepository,
	onShareClick: () -> Unit,
	onInfoClick: () -> Unit,
	onAboutClick: () -> Unit,
	onExportClick: () -> Unit,
	onImportClick: () -> Unit,
	onSettingsClick: () -> Unit,
	externalRequests: ExternalRequests = ExternalRequests(),
) {
	val pagerState =
		rememberPagerState(
			initialPage = ScreenPages.Fasting.ordinal,
			pageCount = { ScreenPages.entries.size })
	val coroutineScope = rememberCoroutineScope()

	var showMenu by remember { mutableStateOf(false) }
	val shareEnabled = remember { mutableStateOf(repository.getFastStart() != null) }

	val fastingTitle = stringResource(id = R.string.title_fasting)
	val logTitle = stringResource(id = R.string.title_log)
	val profileTitle = stringResource(id = R.string.title_profile)

	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val compactHeight = windowSizeClass.minHeightDp < windowSizeClass.minWidthDp

	val currentTitle = remember(pagerState.currentPage, fastingTitle, logTitle, profileTitle) {
		when (ScreenPages.fromOrdinal(pagerState.currentPage)) {
			ScreenPages.Fasting -> fastingTitle
			ScreenPages.Log -> logTitle
			ScreenPages.Profile -> profileTitle
		}
	}

	LaunchedEffect(repository.isFasting()) {
		shareEnabled.value = repository.getFastStart() != null
	}

	Scaffold(
		topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				),
				modifier = Modifier.Companion
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.primary),
				title = {
					Text(
						text = currentTitle,
						style = MaterialTheme.typography.headlineMedium,
					)
				},
				actions = {
					IconButton(
						onClick = onShareClick,
						enabled = shareEnabled.value
					) {
						Icon(
							imageVector = Icons.Default.Share,
							contentDescription = stringResource(id = R.string.action_share),
						)
					}

					IconButton(onClick = onInfoClick) {
						Icon(
							imageVector = Icons.Default.Info,
							contentDescription = stringResource(id = R.string.action_info),
						)
					}

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
							onClick = {
								onAboutClick()
								showMenu = false
							},
						)

						DropdownMenuItem(
							text = { Text(stringResource(id = R.string.action_settings)) },
							onClick = {
								onSettingsClick()
								showMenu = false
							},
						)

						DropdownMenuItem(
							text = { Text(stringResource(id = R.string.action_export)) },
							onClick = {
								onExportClick()
								showMenu = false
							},
						)

						DropdownMenuItem(
							text = { Text(stringResource(id = R.string.action_import)) },
							onClick = {
								onImportClick()
								showMenu = false
							},
						)
					}
				}
			)
		},
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
			Box(
				modifier = Modifier
					.fillMaxSize()
			) {
				Content(
					Modifier.fillMaxSize(),
					contentPaddingValues = paddingValues,
					pagerState,
					externalRequests,
				)
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
