package com.airposted.bitoronbd.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.airposted.bitoronbd.data.repositories.UserRepository
import okhttp3.MultipartBody


class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun checkNumber(
        mobile: String
    ) = withContext(Dispatchers.IO) { repository.numberCheck(mobile) }

    /*suspend fun userLogin(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) { repository.userLogin(email, password) }*/

    suspend fun userSignup(
        name: String,
        phone: String
    ) = withContext(Dispatchers.IO) { repository.userSignup(name, phone) }

    suspend fun userSignupWithPhoto(
        name: String,
        phone: String,
        photo: MultipartBody.Part,
        photo_name: String
    ) = withContext(Dispatchers.IO) { repository.userSignupWithPhoto(name, phone, photo, photo_name) }

}