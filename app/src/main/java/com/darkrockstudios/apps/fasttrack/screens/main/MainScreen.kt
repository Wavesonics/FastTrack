package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.activefast.ActiveFastRepositoryImpl
import com.darkrockstudios.apps.fasttrack.screens.fasting.FastingScreen
import com.darkrockstudios.apps.fasttrack.screens.log.LogScreen
import com.darkrockstudios.apps.fasttrack.screens.profile.ProfileScreen
import com.darkrockstudios.apps.fasttrack.utils.Utils
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
@Composable
fun MainScreen(
	repository: ActiveFastRepositoryImpl,
	onShareClick: () -> Unit,
	onInfoClick: () -> Unit,
	onAboutClick: () -> Unit,
) {
	val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
	val coroutineScope = rememberCoroutineScope()

	var currentTitle by remember { mutableStateOf("") }
	var showMenu by remember { mutableStateOf(false) }
	val shareEnabled = remember { mutableStateOf(repository.getFastStart() != null) }

	val fastingTitle = stringResource(id = R.string.title_fasting)
	val logTitle = stringResource(id = R.string.title_log)
	val profileTitle = stringResource(id = R.string.title_profile)

	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val compactHeight = windowSizeClass.isHeightAtLeastBreakpoint(600).not()

	LaunchedEffect(pagerState.currentPage) {
		currentTitle = when (pagerState.currentPage) {
			0 -> fastingTitle
			1 -> logTitle
			2 -> profileTitle
			else -> ""
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
						selected = pagerState.currentPage == 0,
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

			Row(modifier = Modifier
				.padding(top = paddingValues.calculateTopPadding())
				.fillMaxSize()) {
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
					pagerState
				)
			}
		} else {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
			) {
				Content(
					Modifier.fillMaxSize(),
					contentPaddingValues = PaddingValues(0.dp),
					pagerState,
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
) {
	HorizontalPager(
		modifier = modifier,
		state = pagerState,
		key = { page -> page }
	) { page ->
		PageContainer(
			page = page,
			contentPaddingValues = contentPaddingValues,
		)
	}
}

@ExperimentalTime
@Composable
private fun PageContainer(
	page: Int,
	contentPaddingValues: PaddingValues,
) {
	when (page) {
		0 -> {
			FastingScreen(contentPaddingValues)
		}

		1 -> {
			LogScreen(contentPaddingValues)
		}

		2 -> {
			val context = LocalContext.current
			ProfileScreen(
				contentPaddingValues = contentPaddingValues,
				onShowInfoDialog = { titleRes, contentRes ->
					// Show info dialog
					Utils.showInfoDialog(
						titleRes,
						contentRes,
						context
					)
				}
			)
		}

		else -> {
			error("Unknown page: $page")
		}
	}
}
