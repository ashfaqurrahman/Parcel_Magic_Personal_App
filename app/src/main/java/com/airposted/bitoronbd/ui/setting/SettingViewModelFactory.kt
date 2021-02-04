package com.airposted.bitoronbd.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airposted.bitoronbd.data.repositories.SettingRepository

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory(
    private val repository: SettingRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingViewModel(repository) as T
    }
}