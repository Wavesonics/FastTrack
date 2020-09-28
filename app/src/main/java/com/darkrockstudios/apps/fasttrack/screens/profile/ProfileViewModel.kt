package com.darkrockstudios.apps.fasttrack.screens.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.darkrockstudios.apps.fasttrack.data.Profile

class ProfileViewModel: ViewModel()
{
	val profile = MutableLiveData<Profile>()
}