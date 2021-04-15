package com.airposted.bitoronbd.ui.history

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.OrderHistoryListRepository
import com.airposted.bitoronbd.model.SetParcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyParcelHistoryViewModel(
    private val repository: OrderHistoryListRepository
) : ViewModel() {

    suspend fun getOrderList(
        order: Int
    ) = withContext(Dispatchers.IO) { repository.getOrders(order) }

    suspend fun setOrder(
        setParcel: SetParcel
    ) = withContext(Dispatchers.IO) { repository.setOrder(setParcel) }
}