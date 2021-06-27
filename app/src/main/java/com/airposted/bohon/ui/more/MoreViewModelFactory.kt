package com.airposted.bohon.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bohon.data.repositories.MoreRepository

@Suppress("UNCHECKED_CAST")
class MoreViewModelFactory(
    private val repository: MoreRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MoreViewModel(repository) as T
    }
}