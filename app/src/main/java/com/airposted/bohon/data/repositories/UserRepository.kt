package com.airposted.bohon.data.repositories

import com.airposted.bohon.data.network.MyApi
import com.airposted.bohon.data.network.SafeApiRequest
import com.airposted.bohon.data.network.responses.AuthResponse
import com.airposted.bohon.model.SearchLocation
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository(
    private val api: MyApi,
    //private val db: AppDatabase
) : SafeApiRequest() {

    suspend fun numberCheck(mobile: String): AuthResponse {
        return apiRequest { api.numberCheck(mobile) }
    }

    suspend fun locationSearch(place: String): SearchLocation {
        return apiRequest { api.getPlacesNameList(place) }
    }

    /*suspend fun userLogin(email: String, password: String): AuthResponse {
        return apiRequest { api.userLogin(email, password) }
    }*/

    suspend fun userSignup(
        name: String,
        phone: String
    ) : AuthResponse {
        return apiRequest{ api.userSignup(name, phone)}
    }

    suspend fun userSignupWithPhoto(
        name: RequestBody,
        phone: RequestBody,
        photo: MultipartBody.Part,
        photo_name: RequestBody
    ) : AuthResponse {
        return apiRequest{ api.userSignupWithPhoto(name, phone, photo, photo_name)}
    }

    /*suspend fun saveUser(user: User) = db.getUserDao().upsert(user)

    fun getUser() = db.getUserDao().getuser()*/

}