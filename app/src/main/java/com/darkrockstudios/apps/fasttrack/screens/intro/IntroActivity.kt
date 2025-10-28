package com.darkrockstudios.apps.fasttrack.screens.intro

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Data
import com.darkrockstudios.apps.fasttrack.ui.theme.FastTrackTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class IntroActivity : AppCompatActivity() {
    private val storage by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
	private lateinit var requestNotificationPermission: ActivityResultLauncher<String>
	private var shouldRequestPermission by mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
	    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

	    registerNotificationPermissionCallback()

        setContent {
            FastTrackTheme {
                IntroScreen(
	                onComplete = { complete() },
	                onNotificationSlideExited = { requestNotificationPermissionIfNeeded() }
                )
            }
        }
    }

	private fun registerNotificationPermissionCallback() {
		requestNotificationPermission = registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted: Boolean ->
			if (isGranted) {
				Napier.d("Notification permission granted")
			} else {
				Napier.w("Notification permission denied")
			}
			shouldRequestPermission = false
		}
	}

	private fun requestNotificationPermissionIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			when {
				ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED -> {
					Napier.d("Notification permission already granted")
				}

				shouldRequestPermission -> {
					// Already requested, don't request again
					Napier.d("Notification permission already requested")
				}

				else -> {
					Napier.d("Requesting notification permission")
					shouldRequestPermission = true
					requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
				}
			}
		}
	}

    private fun complete() {
        storage.edit {
            putBoolean(Data.KEY_INTRO_SEEN, true)
        }
        finish()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
	onComplete: () -> Unit,
	onNotificationSlideExited: () -> Unit
) {
	val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()
	var hasRequestedPermission by remember { mutableStateOf(false) }

	// Watch for page changes and request permission when moving past slide 4
	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.currentPage }.collect { page ->
			if (page > 4 && !hasRequestedPermission) {
				hasRequestedPermission = true
				onNotificationSlideExited()
			}
		}
	}

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> IntroSlide(
                    title = stringResource(id = R.string.intro_00_title),
                    description = stringResource(id = R.string.intro_00_description),
                    imageDrawable = R.drawable.intro_00,
                    backgroundColor = Color.rgb(128, 91, 128) // Pastel Magenta
                )

                1 -> IntroSlide(
                    title = stringResource(id = R.string.intro_01_title),
                    description = stringResource(id = R.string.intro_01_description),
                    imageDrawable = R.drawable.intro_01,
                    backgroundColor = Color.rgb(128, 91, 96) // Pastel Red
                )

                2 -> IntroSlide(
                    title = stringResource(id = R.string.intro_02_title),
                    description = stringResource(id = R.string.intro_02_description),
                    imageDrawable = R.drawable.intro_02,
                    backgroundColor = Color.rgb(86, 108, 115) // Pastel Blue
                )

                3 -> IntroSlide(
                    title = stringResource(id = R.string.intro_03_title),
                    description = stringResource(id = R.string.intro_03_description),
                    imageDrawable = R.drawable.intro_03,
                    backgroundColor = Color.rgb(110, 110, 110) // Pastel Gray
                )

                4 -> IntroSlide(
                    title = stringResource(id = R.string.intro_04_title),
                    description = stringResource(id = R.string.intro_04_description),
                    imageDrawable = R.drawable.intro_04,
	                backgroundColor = Color.rgb(96, 128, 91) // Pastel Green
                )

	            5 -> IntroSlide(
		            title = stringResource(id = R.string.intro_05_title),
		            description = stringResource(id = R.string.intro_05_description),
		            imageDrawable = R.drawable.intro_05,
                    backgroundColor = Color.rgb(128, 91, 128) // Pastel Magenta
                )
            }
        }

        // Navigation controls at the bottom
        Column(
            modifier = Modifier
	            .align(Alignment.BottomCenter)
	            .padding(16.dp)
	            .fillMaxWidth()
        ) {
            // Page indicator dots
            Row(
                modifier = Modifier
	                .fillMaxWidth()
	                .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
	            repeat(6) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        androidx.compose.ui.graphics.Color.White
                    } else {
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                    }
                    Box(
                        modifier = Modifier
	                        .padding(4.dp)
	                        .size(8.dp)
	                        .background(color = color, shape = MaterialTheme.shapes.small)
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
	                .fillMaxWidth()
	                .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous button or Skip button
                if (pagerState.currentPage > 0) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.previous_button))
                    }
                } else {
                    // Skip button
                    Button(
                        onClick = { onComplete() }
                    ) {
                        Text(stringResource(id = R.string.skip_button))
                    }
                }

                // Next button or Done button
	            if (pagerState.currentPage < 5) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.next_button))
                    }
                } else {
                    // Done button
                    Button(
                        onClick = { onComplete() }
                    ) {
                        Text(stringResource(id = R.string.done_button))
                    }
                }
            }
        }
    }
}

@Composable
fun IntroSlide(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    imageDrawable: Int,
    backgroundColor: Int
) {
    Box(
        modifier = modifier
	        .fillMaxSize()
	        .background(color = androidx.compose.ui.graphics.Color(backgroundColor))
    ) {
        Column(
            modifier = Modifier
	            .fillMaxSize()
	            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageDrawable),
                contentDescription = null,
                modifier = Modifier
	                .size(200.dp)
	                .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 16.sp,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
