package com.darkrockstudios.apps.fasttrack.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.darkrockstudios.apps.fasttrack.R

private const val GITHUB_URL = "https://github.com/Darkrock-Studios"
private const val WEBSITE_URL = "https://darkrock.studio/"
private const val DISCORD_URL = "https://discord.gg/ju2RQa5x8W"

/**
 * The About dialog in Compose, replacing the retired MaterialAbout library
 * (the last dependency that needed Jetifier). Cover photo with the studio
 * avatar overlapping its lower edge, link chips, then the app block with
 * rate and share actions. Spacing follows the app's Fibonacci scale.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutDialog(
	versionName: String,
	onOpenUrl: (String) -> Unit,
	onRateApp: () -> Unit,
	onShareApp: () -> Unit,
	onDismiss: () -> Unit,
) {
	Dialog(onDismissRequest = onDismiss) {
		val cardColor = CardDefaults.cardColors().containerColor

		Card(modifier = Modifier.widthIn(max = 420.dp)) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				// Cover photo with the avatar overlapping its lower edge
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(188.dp)
				) {
					Image(
						painter = painterResource(id = R.mipmap.profile_cover),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxWidth()
							.height(144.dp)
							.align(Alignment.TopCenter)
					)

					IconButton(
						onClick = onDismiss,
						modifier = Modifier
							.align(Alignment.TopEnd)
							.padding(8.dp),
						colors = IconButtonDefaults.iconButtonColors(
							containerColor = Color.Black.copy(alpha = 0.35f),
							contentColor = Color.White,
						),
					) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = stringResource(id = R.string.close_button_content_description),
						)
					}

					Image(
						painter = painterResource(id = R.drawable.darkrockstudios_logo),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.size(89.dp)
							.clip(CircleShape)
							.border(3.dp, cardColor, CircleShape)
					)
				}

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = stringResource(id = R.string.about_name),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.SemiBold,
				)
				Text(
					text = stringResource(id = R.string.about_subtitle),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)

				Spacer(modifier = Modifier.height(13.dp))

				Text(
					text = stringResource(id = R.string.about_brief),
					style = MaterialTheme.typography.bodyMedium,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(horizontal = 34.dp),
				)

				Spacer(modifier = Modifier.height(13.dp))

				// Link chips wrap on narrow screens instead of overflowing
				FlowRow(
					modifier = Modifier.padding(horizontal = 21.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
				) {
					AssistChip(
						onClick = { onOpenUrl(GITHUB_URL) },
						label = { Text(stringResource(id = R.string.about_github)) },
						leadingIcon = {
							Icon(
								imageVector = Icons.Default.Code,
								contentDescription = null,
								modifier = Modifier.size(AssistChipDefaults.IconSize),
							)
						},
					)
					AssistChip(
						onClick = { onOpenUrl(WEBSITE_URL) },
						label = { Text(stringResource(id = R.string.about_website)) },
						leadingIcon = {
							Icon(
								imageVector = Icons.Default.Language,
								contentDescription = null,
								modifier = Modifier.size(AssistChipDefaults.IconSize),
							)
						},
					)
					AssistChip(
						onClick = { onOpenUrl(DISCORD_URL) },
						label = { Text(stringResource(id = R.string.about_discord)) },
						leadingIcon = {
							Icon(
								painter = painterResource(id = R.drawable.ic_discord),
								contentDescription = null,
								modifier = Modifier.size(AssistChipDefaults.IconSize),
							)
						},
					)
				}

				Spacer(modifier = Modifier.height(13.dp))

				HorizontalDivider(modifier = Modifier.padding(horizontal = 21.dp))

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 21.dp, vertical = 13.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Image(
						painter = painterResource(id = R.drawable.app_icon),
						contentDescription = null,
						modifier = Modifier.size(55.dp),
					)
					Spacer(modifier = Modifier.width(13.dp))
					Column {
						Text(
							text = stringResource(id = R.string.app_name),
							style = MaterialTheme.typography.titleMedium,
						)
						Text(
							text = "v$versionName",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
				}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(start = 21.dp, end = 21.dp, bottom = 21.dp)
				) {
					Button(
						onClick = onRateApp,
						modifier = Modifier
							.fillMaxWidth()
							.heightIn(min = 48.dp),
					) {
						Icon(
							imageVector = Icons.Default.Star,
							contentDescription = null,
							modifier = Modifier.size(18.dp),
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(stringResource(id = R.string.about_rate))
					}

					Spacer(modifier = Modifier.height(8.dp))

					OutlinedButton(
						onClick = onShareApp,
						modifier = Modifier
							.fillMaxWidth()
							.heightIn(min = 48.dp),
					) {
						Icon(
							imageVector = Icons.Default.Share,
							contentDescription = null,
							modifier = Modifier.size(18.dp),
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(stringResource(id = R.string.action_share))
					}
				}
			}
		}
	}
}
