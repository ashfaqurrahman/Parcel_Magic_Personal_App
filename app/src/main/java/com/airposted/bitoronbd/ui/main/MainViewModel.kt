package com.airposted.bitoronbd.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.MainRepository
import com.airposted.bitoronbd.utils.lazyDeferred

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val locationLiveData = LocationLiveData(application)
    internal fun getLocationLiveData() = locationLiveData

}