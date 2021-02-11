package com.airposted.bitoronbd.ui.location_set

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bitoronbd.data.repositories.LocationSetRepository

@Suppress("UNCHECKED_CAST")
class LocationSetViewModelFactory(
    private val repository: LocationSetRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LocationSetViewModel(repository) as T
    }
}