package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.lifecycle.ViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class ManualAddViewModel: ViewModel()
{
	var startDate: LocalDate? = null
	var startDateTime: LocalDateTime? = null
	var length: Long? = null
}