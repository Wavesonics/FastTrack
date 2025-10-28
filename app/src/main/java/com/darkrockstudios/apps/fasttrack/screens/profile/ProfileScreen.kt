package com.darkrockstudios.apps.fasttrack.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.Gender
import com.darkrockstudios.apps.fasttrack.utils.MAX_COLUMN_WIDTH
import com.darkrockstudios.apps.fasttrack.utils.combinePadding
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
	contentPaddingValues: PaddingValues = PaddingValues(0.dp),
	viewModel: IProfileViewModel = koinViewModel<ProfileViewModel>(),
	onShowInfoDialog: (titleRes: Int, contentRes: Int) -> Unit
) {
	val uiState by viewModel.uiState.collectAsState(initial = IProfileViewModel.ProfileUiState())

	LaunchedEffect(Unit) {
		viewModel.onCreate()
	}
	Box(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.widthIn(max = MAX_COLUMN_WIDTH)
				.fillMaxHeight()
				.align(Alignment.Center)
				.verticalScroll(rememberScrollState())
				.padding(combinePadding(contentPaddingValues, PaddingValues(16.dp)))
		) {
			BmiCard(
				bmiValue = uiState.bmiValue,
				onInfoClick = {
					onShowInfoDialog(
						R.string.info_dialog_bmi_title,
						R.string.info_dialog_bmi_content
					)
				}
			)

			Spacer(modifier = Modifier.height(16.dp))

			BmrCard(
				bmrValue = uiState.bmrValue,
				onInfoClick = {
					onShowInfoDialog(
						R.string.info_dialog_bmr_title,
						R.string.info_dialog_bmr_content
					)
				}
			)

			Spacer(modifier = Modifier.height(16.dp))

			ProfileDataEntryCard(
				isMetric = uiState.isMetric,
				heightCm = uiState.heightCm,
				heightFeet = uiState.heightFeet,
				heightInches = uiState.heightInches,
				weightKg = uiState.weightKg,
				weightLbs = uiState.weightLbs,
				age = uiState.age,
				gender = uiState.gender,
				heightCmError = uiState.heightCmError,
				heightFeetError = uiState.heightFeetError,
				heightInchesError = uiState.heightInchesError,
				weightKgError = uiState.weightKgError,
				weightLbsError = uiState.weightLbsError,
				ageError = uiState.ageError,
				onHeightCmChanged = viewModel::updateHeightCm,
				onHeightFeetChanged = viewModel::updateHeightFeet,
				onHeightInchesChanged = viewModel::updateHeightInches,
				onWeightKgChanged = viewModel::updateWeightKg,
				onWeightLbsChanged = viewModel::updateWeightLbs,
				onAgeChanged = viewModel::updateAge,
				onGenderChanged = viewModel::updateGender,
				onMetricSwitchChanged = viewModel::updateMetricSystem
			)
		}
	}
}

@Composable
fun BmiCard(
	bmiValue: String,
	onInfoClick: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onInfoClick) {
					Icon(
						painter = painterResource(id = R.drawable.ic_more_info),
						contentDescription = stringResource(id = R.string.bmi_info_button_description)
					)
				}
				Text(
					text = stringResource(id = R.string.profile_bmi_label),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}

			Text(
				text = bmiValue,
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = 16.dp)
			)
		}
	}
}

@Composable
fun BmrCard(
	bmrValue: String,
	onInfoClick: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onInfoClick) {
					Icon(
						painter = painterResource(id = R.drawable.ic_more_info),
						contentDescription = stringResource(id = R.string.bmr_info_button_description)
					)
				}
				Text(
					text = stringResource(id = R.string.profile_bmr_label),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}

			Text(
				text = bmrValue,
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = 16.dp)
			)
		}
	}
}

@Composable
fun ProfileDataEntryCard(
	isMetric: Boolean,
	heightCm: String,
	heightFeet: String,
	heightInches: String,
	weightKg: String,
	weightLbs: String,
	age: String,
	gender: Gender,
	heightCmError: String?,
	heightFeetError: String?,
	heightInchesError: String?,
	weightKgError: String?,
	weightLbsError: String?,
	ageError: String?,
	onHeightCmChanged: (String) -> Unit,
	onHeightFeetChanged: (String) -> Unit,
	onHeightInchesChanged: (String) -> Unit,
	onWeightKgChanged: (String) -> Unit,
	onWeightLbsChanged: (String) -> Unit,
	onAgeChanged: (String) -> Unit,
	onGenderChanged: (Gender) -> Unit,
	onMetricSwitchChanged: (Boolean) -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = stringResource(id = R.string.profile_height_label),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(top = 16.dp)
				)

				Row(
					horizontalArrangement = Arrangement.End,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(id = R.string.profile_metric_switch),
						modifier = Modifier.padding(end = 8.dp),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onSurface,
					)
					Switch(
						checked = isMetric,
						onCheckedChange = onMetricSwitchChanged
					)
				}
			}

			if (isMetric) {
				// Metric Height Input
				OutlinedTextField(
					value = heightCm,
					onValueChange = onHeightCmChanged,
					label = { Text(stringResource(id = R.string.profile_height_metric_label)) },
					isError = heightCmError != null,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier
						.padding(top = 8.dp)
						.width(120.dp)
				)
				if (heightCmError != null) {
					Text(
						text = heightCmError,
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(start = 16.dp)
					)
				}
			} else {
				// Imperial Height Input
				Row(
					modifier = Modifier.padding(top = 8.dp)
				) {
					OutlinedTextField(
						value = heightFeet,
						onValueChange = onHeightFeetChanged,
						label = { Text(stringResource(id = R.string.profile_height_imper_feet_label)) },
						isError = heightFeetError != null,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						modifier = Modifier.width(100.dp)
					)

					Spacer(modifier = Modifier.width(8.dp))

					OutlinedTextField(
						value = heightInches,
						onValueChange = onHeightInchesChanged,
						label = { Text(stringResource(id = R.string.profile_height_imper_inches_label)) },
						isError = heightInchesError != null,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						modifier = Modifier.width(120.dp)
					)
				}

				if (heightFeetError != null) {
					Text(
						text = heightFeetError,
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(start = 16.dp)
					)
				}

				if (heightInchesError != null) {
					Text(
						text = heightInchesError,
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(start = 16.dp)
					)
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				// Weight Section
				Column {
					Text(
						text = stringResource(id = R.string.profile_weight_label),
						style = MaterialTheme.typography.headlineMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(top = 16.dp)
					)

					if (isMetric) {
						OutlinedTextField(
							value = weightKg,
							onValueChange = onWeightKgChanged,
							label = { Text(stringResource(id = R.string.profile_weight_kg_label)) },
							isError = weightKgError != null,
							keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
							modifier = Modifier
								.padding(top = 8.dp)
								.width(120.dp)
						)

						if (weightKgError != null) {
							Text(
								text = weightKgError,
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.bodySmall,
								modifier = Modifier.padding(start = 16.dp)
							)
						}
					} else {
						OutlinedTextField(
							value = weightLbs,
							onValueChange = onWeightLbsChanged,
							label = { Text(stringResource(id = R.string.profile_weight_pounds_label)) },
							isError = weightLbsError != null,
							keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
							modifier = Modifier
								.padding(top = 8.dp)
								.width(120.dp)
						)

						if (weightLbsError != null) {
							Text(
								text = weightLbsError,
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.bodySmall,
								modifier = Modifier.padding(start = 16.dp)
							)
						}
					}
				}

				// Age Section
				Column {
					Text(
						text = stringResource(id = R.string.profile_age_label),
						style = MaterialTheme.typography.headlineMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(top = 16.dp)
					)

					OutlinedTextField(
						value = age,
						onValueChange = onAgeChanged,
						label = { Text(stringResource(id = R.string.profile_age_years)) },
						isError = ageError != null,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						modifier = Modifier
							.padding(top = 8.dp)
							.width(100.dp)
					)

					if (ageError != null) {
						Text(
							text = ageError,
							color = MaterialTheme.colorScheme.error,
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.padding(start = 16.dp)
						)
					}
				}
			}

			// Gender Section
			Text(
				text = stringResource(id = R.string.profile_gender_label),
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(top = 16.dp)
			)

			Row(
				modifier = Modifier.padding(top = 8.dp)
			) {
				RadioButton(
					selected = gender == Gender.Male,
					onClick = { onGenderChanged(Gender.Male) }
				)
				Text(
					text = stringResource(id = R.string.profile_gender_male),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.padding(end = 16.dp)
						.align(Alignment.CenterVertically)
				)

				RadioButton(
					selected = gender == Gender.Female,
					onClick = { onGenderChanged(Gender.Female) }
				)
				Text(
					text = stringResource(id = R.string.profile_gender_female),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.align(Alignment.CenterVertically)
				)
			}
		}
	}
}
