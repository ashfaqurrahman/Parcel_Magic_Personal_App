package com.airposted.bitoronbd.ui.more

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.MoreRepository
import com.airposted.bitoronbd.utils.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MoreViewModel (
    private val repository: MoreRepository
) : ViewModel() {

    val getName by lazyDeferred {
        repository.getName()
    }

    val getImage by lazyDeferred {
        repository.getImage()
    }

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