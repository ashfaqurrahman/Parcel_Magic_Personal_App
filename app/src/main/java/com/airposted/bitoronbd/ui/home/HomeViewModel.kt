package com.airposted.bitoronbd.ui.home

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.HomeRepository
import com.airposted.bitoronbd.data.repositories.MainRepository
import com.airposted.bitoronbd.utils.lazyDeferred

class HomeViewModel(
    repository: HomeRepository
) : ViewModel() {

    val getLat by lazyDeferred {
        repository.getLat()
    }

    val getLong by lazyDeferred {
        repository.getLong()
    }

}