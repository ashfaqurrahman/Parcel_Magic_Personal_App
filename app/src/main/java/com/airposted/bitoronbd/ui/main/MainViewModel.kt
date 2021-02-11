package com.airposted.bitoronbd.ui.main

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.MainRepository
import com.airposted.bitoronbd.utils.lazyDeferred

class MainViewModel(
    private val repository: MainRepository
) : ViewModel() {

    val gps = repository.location()

    val network = repository.internet()

    val currentLocation = repository.currentLocation()

}