package com.airposted.bitoronbd.ui.home

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.HomeRepository
import com.airposted.bitoronbd.utils.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    suspend fun getSetting() = withContext(Dispatchers.IO) { repository.getSetting() }

    val getName by lazyDeferred {
        repository.getName()
    }

    val gps = repository.location()

    val network = repository.internet()

    val currentLocation = repository.currentLocation()

}