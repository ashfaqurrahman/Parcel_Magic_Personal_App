package com.airposted.bitoronbd.data.repositories

import android.content.Context
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.OrderListData

class OrderListRepository(context: Context, private val api: MyApi) : SafeApiRequest() {

    suspend fun getOrders(
        header: String,
        order: Int
    ) : OrderListData {
        var response:OrderListData? = null
        when(order){
            0-> {
                response = apiRequest { api.currentOrderList(header)}
            }
            1-> {
                response = apiRequest { api.pendingOrderList(header)}
            }
        }

        return response!!
    }
}