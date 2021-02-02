package com.airposted.bitoronbd.data.repositories

import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.responses.AuthResponse

class UserRepository(
    private val api: MyApi,
    //private val db: AppDatabase
) : SafeApiRequest() {

    suspend fun numberCheck(mobile: String): AuthResponse {
        return apiRequest { api.numberCheck(mobile) }
    }

    /*suspend fun userLogin(email: String, password: String): AuthResponse {
        return apiRequest { api.userLogin(email, password) }
    }*/

    suspend fun userSignup(
        name: String,
        email: String,
        password: String
    ) : AuthResponse {
        return apiRequest{ api.userSignup(name, email, password)}
    }

    /*suspend fun saveUser(user: User) = db.getUserDao().upsert(user)

    fun getUser() = db.getUserDao().getuser()*/

}