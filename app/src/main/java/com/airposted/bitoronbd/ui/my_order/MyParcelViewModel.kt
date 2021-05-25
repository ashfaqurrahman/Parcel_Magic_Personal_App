package com.airposted.bitoronbd.ui.my_order

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.OrderListRepository
import com.airposted.bitoronbd.model.SetParcel
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
}