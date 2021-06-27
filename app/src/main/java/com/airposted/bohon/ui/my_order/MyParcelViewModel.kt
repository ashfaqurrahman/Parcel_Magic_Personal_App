package com.airposted.bohon.ui.my_order

import androidx.lifecycle.ViewModel
import com.airposted.bohon.data.repositories.OrderListRepository
import com.airposted.bohon.model.SetParcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyParcelViewModel(
    private val repository: OrderListRepository
) : ViewModel() {

    suspend fun getOrderList(
        order: Int
    ) = withContext(Dispatchers.IO) { repository.getOrders(order) }

    suspend fun setOrder(
        setParcel: SetParcel
    ) = withContext(Dispatchers.IO) { repository.setOrder(setParcel) }

    suspend fun changeStatus(invoice: String, status: Int) = withContext(Dispatchers.IO) { repository.changeStatus(invoice, status) }

    suspend fun getDirections(
        url: String
    ) = withContext(Dispatchers.IO) { repository.directionSearch(url) }
}