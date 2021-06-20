package com.airposted.bitoronbd.ui.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airposted.bitoronbd.data.db.Location
import com.airposted.bitoronbd.data.repositories.HomeRepository
import com.airposted.bitoronbd.utils.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val locations = repository.getAllLocations()
    val getLastLocation = repository.getLastLocation()

    val runs = MediatorLiveData<List<String>>()

    init {
        runs.addSource(locations) {
            it.let {
                runs.value = it
            }
        }
    }

    suspend fun getSetting() = withContext(Dispatchers.IO) { repository.getSetting() }

    val getName by lazyDeferred {
        repository.getName()
    }

    val gps = repository.location()

    val network = repository.internet()

    val currentLocation = repository.currentLocation()

    suspend fun saveFcmToken(fcm_token: String) = withContext(Dispatchers.IO) { repository.saveFcmToken(fcm_token) }
    suspend fun deleteFcmToken() = withContext(Dispatchers.IO) { repository.deleteFcmToken() }

    fun saveAddress(run: Location) = viewModelScope.launch {
        repository.saveAddress(run)
    }

}