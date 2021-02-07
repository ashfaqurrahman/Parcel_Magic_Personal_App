package com.airposted.bitoronbd.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MoreRepository(context: Context, private val api: MyApi) : SafeApiRequest() {
    private val userName = MutableLiveData<String>()
    private val userPhone = MutableLiveData<String>()
    private val userImage = MutableLiveData<String>()
    private val userToken = MutableLiveData<String>()
    private val appContext = context.applicationContext

    /*init {
        userName.observeForever{
            saveName(it)
        }
        userPhone.observeForever{
            savePhone(it)
        }
        userImage.observeForever{
            saveImage(it)
        }
        userToken.observeForever{
            saveToken(it)
        }
    }*/

    suspend fun getName(): LiveData<String> {
        return withContext(Dispatchers.IO) {
            fetchName()
        }
    }

    private fun fetchName(): LiveData<String> {
        userName.postValue(PersistentUser.getInstance().getFullName(appContext))
        return userName
    }

    suspend fun userNameUpdate(
        header: String,
        name: String
    ) : String {
        val response = apiRequest{ api.userNameUpdate(header, name)}
        if (response.success){
            PersistentUser.getInstance().setFullname(appContext, response.user?.name)
            userName.postValue(response.user?.name)
        }
        return response.msg
    }

    suspend fun getImage(): LiveData<String> {
        return withContext(Dispatchers.IO) {
            fetchImage()
        }
    }

    private fun fetchImage(): LiveData<String> {
        userImage.postValue(PersistentUser.getInstance().getUserImage(appContext))
        return userImage
    }

    suspend fun userImageUpdate(
        header: String,
        photo: MultipartBody.Part,
        photo_name: RequestBody
    ) : String {
        val response = apiRequest{ api.userImageUpdate(header, photo, photo_name)}
        if (response.success){
            PersistentUser.getInstance().setUserImage(appContext, "https://parcel.airposted.com/img/users/" + response.user?.image)
            userImage.postValue("https://parcel.airposted.com/img/users/" + response.user?.image)
        }
        return response.msg
    }
}