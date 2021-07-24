package com.airposted.bohon.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airposted.bohon.data.db.Location
import com.airposted.bohon.data.repositories.HomeRepository
import com.airposted.bohon.utils.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    val locations = repository.getAllLocations()
    val getLastLocation = repository.getLastLocation()

    suspend fun getSetting() = withContext(Dispatchers.IO) { repository.getSetting() }

    val gps = repository.location()

    val network = repository.internet()

    val currentLocation = repository.currentLocation()

    suspend fun saveFcmToken(fcm_token: String) = withContext(Dispatchers.IO) { repository.saveFcmToken(fcm_token) }
    suspend fun deleteFcmToken() = withContext(Dispatchers.IO) { repository.deleteFcmToken() }
    suspend fun getUserBasedCoupons() = withContext(Dispatchers.IO) { repository.getUserBasedCoupons() }

    fun saveAddress(run: Location) = viewModelScope.launch {
        repository.saveAddress(run)
    }

    val name = repository.userName
    val image = repository.userImage

    suspend fun userNameUpdate(
        header: String,
        name: String
    ) = withContext(Dispatchers.IO) { repository.userNameUpdate(header, name) }

    suspend fun userImageUpdate(
        header: String,
        photo: MultipartBody.Part,
        photo_name: RequestBody
    ) = withContext(Dispatchers.IO) { repository.userImageUpdate(header, photo, photo_name) }

}