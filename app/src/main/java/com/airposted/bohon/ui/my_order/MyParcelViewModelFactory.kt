package com.airposted.bohon.ui.my_order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bohon.data.repositories.OrderListRepository

@Suppress("UNCHECKED_CAST")
class MyParcelViewModelFactory(
    private val repository: OrderListRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyParcelViewModel(repository) as T
    }
}