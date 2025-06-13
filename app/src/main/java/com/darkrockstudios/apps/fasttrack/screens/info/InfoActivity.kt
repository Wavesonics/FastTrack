package com.darkrockstudios.apps.fasttrack.screens.info

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme

class InfoActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		// Set the Compose content
		setContent {
			FastTrackTheme {
				Scaffold(
					topBar = {
						TopAppBar(
							colors = TopAppBarDefaults.topAppBarColors(
								containerColor = MaterialTheme.colorScheme.primaryContainer,
								titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
								actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
								navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
							),
							modifier = Modifier
								.fillMaxWidth(),
							title = { Text(stringResource(id = R.string.action_info)) },
							navigationIcon = {
								IconButton(onClick = { onBackPressed() }) {
									Icon(
										imageVector = Icons.Default.ArrowBack,
										contentDescription = "Back"
									)
								}
							}
						)
					}
				) { paddingValues ->
					InfoScreen(
						modifier = Modifier
							.fillMaxSize(),
						paddingValues = paddingValues,
					)
				}
			}
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
}
