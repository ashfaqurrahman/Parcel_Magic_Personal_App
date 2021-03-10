package com.airposted.bitoronbd.ui.my_parcel

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.OrderListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyParcelViewModel(
    private val repository: OrderListRepository
) : ViewModel() {

    suspend fun getOrderList(
        header: String,
        order: Int
    ) = withContext(Dispatchers.IO) { repository.getOrders(header, order) }
}