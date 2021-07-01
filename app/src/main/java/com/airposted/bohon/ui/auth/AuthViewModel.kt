package com.airposted.bohon.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.airposted.bohon.data.repositories.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody


class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun checkNumber(
        mobile: String
    ) = withContext(Dispatchers.IO) { repository.numberCheck(mobile) }

    suspend fun sendOTP(
        mobile: String
    ) = withContext(Dispatchers.IO) { repository.sendOTP(mobile) }

    suspend fun getLocations(
        mobile: String
    ) = withContext(Dispatchers.IO) { repository.locationSearch(mobile) }

    /*suspend fun userLogin(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) { repository.userLogin(email, password) }*/

    suspend fun userSignup(
        name: String,
        phone: String
    ) = withContext(Dispatchers.IO) { repository.userSignup(name, phone) }

    suspend fun userSignupWithPhoto(
        name: RequestBody,
        phone: RequestBody,
        photo: MultipartBody.Part,
        photo_name: RequestBody
    ) = withContext(Dispatchers.IO) { repository.userSignupWithPhoto(name, phone, photo, photo_name) }

}