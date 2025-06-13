package com.darkrockstudios.apps.fasttrack.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.utils.getRawTextFile
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@Composable
fun InfoScreen(
	modifier: Modifier = Modifier,
	paddingValues: PaddingValues = PaddingValues(),
) {
	val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
	val coroutineScope = rememberCoroutineScope()

	Column(
		modifier = modifier
			.padding(top = paddingValues.calculateTopPadding())
			.fillMaxSize()
	) {
		SecondaryTabRow(
			selectedTabIndex = pagerState.currentPage,
			tabs = {
				Tab(
					selected = pagerState.currentPage == 0,
					onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
					text = { Text(stringResource(id = R.string.info_page_title_info)) }
				)

				Tab(
					selected = pagerState.currentPage == 1,
					onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
					text = { Text(stringResource(id = R.string.info_page_title_tips)) }
				)
			})

		HorizontalPager(
			state = pagerState,
			modifier = Modifier.weight(1f)
		) { page ->
			val resourceId = when (page) {
				0 -> R.raw.info
				1 -> R.raw.tips
				else -> throw IllegalArgumentException("Invalid page index")
			}

			InfoContent(resourceId = resourceId, paddingValues = paddingValues)
		}
	}
}

@Composable
fun InfoContent(resourceId: Int, paddingValues: PaddingValues) {
	val context = LocalContext.current
	val markdown = remember(resourceId) {
		context.resources.getRawTextFile(resourceId)
	}

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(state = rememberScrollState())
	) {
		MarkdownText(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
				.padding(top = 8.dp, bottom = paddingValues.calculateBottomPadding()),
			markdown = markdown,
		)
	}
}
