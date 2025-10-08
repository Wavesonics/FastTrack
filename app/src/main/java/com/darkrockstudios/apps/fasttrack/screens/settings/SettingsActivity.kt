package com.darkrockstudios.apps.fasttrack.screens.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.settings.SettingsDatasource
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {
	private val settings by inject<SettingsDatasource>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		WindowCompat.getInsetsController(window, window.decorView)
			.isAppearanceLightStatusBars = false

		setContent {
			FastTrackTheme {
				SettingsScreen(
					onBack = { finish() },
					settings = settings,
				)
			}
		}
	}
}

@Composable
private fun SettingsScreen(
	onBack: () -> Unit,
	settings: SettingsDatasource,
) {
	Scaffold(
		topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				),
				title = { Text(text = stringResource(id = R.string.title_activity_settings)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							Icons.Filled.ArrowBack,
							contentDescription = stringResource(id = R.string.back)
						)
					}
				},
			)
		}
	) { paddingValues ->
		SettingsList(paddingValues = paddingValues, settings = settings)
	}
}

@Composable
private fun SettingsList(paddingValues: PaddingValues, settings: SettingsDatasource) {
	var fancyBackground by remember { mutableStateOf(settings.getShowFancyBackground()) }

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			modifier = Modifier
				.widthIn(max = MAX_COLUMN_WIDTH)
				.fillMaxHeight()
				.align(Alignment.Center),
			contentPadding = paddingValues
		) {
			item(key = "fancy_background") {
				ListItem(
					headlineContent = { Text(text = stringResource(id = R.string.settings_fancy_background_title)) },
					supportingContent = { Text(text = stringResource(id = R.string.settings_fancy_background_subtitle)) },
					trailingContent = {
						Switch(
							checked = fancyBackground,
							onCheckedChange = { checked ->
								fancyBackground = checked
								settings.setShowFancyBackground(checked)
							}
						)
					}
				)
			}
		}
	}
}