package com.airposted.bitoronbd.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bitoronbd.data.repositories.OrderHistoryListRepository

@Suppress("UNCHECKED_CAST")
class MyParcelHistoryViewModelFactory(
    private val repository: OrderHistoryListRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyParcelHistoryViewModel(repository) as T
    }
}