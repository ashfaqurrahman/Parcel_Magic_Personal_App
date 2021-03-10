package com.airposted.bitoronbd.ui.my_parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bitoronbd.data.repositories.OrderListRepository

@Suppress("UNCHECKED_CAST")
class MyParcelViewModelFactory(
    private val repository: OrderListRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyParcelViewModel(repository) as T
    }
}