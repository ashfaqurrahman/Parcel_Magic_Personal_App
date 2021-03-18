package com.airposted.bitoronbd.data.repositories

import android.content.Context
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.OrderListData
import com.airposted.bitoronbd.model.SetParcel
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse

class OrderListRepository(context: Context, private val api: MyApi) : SafeApiRequest() {
    private val appContext = context.applicationContext

    suspend fun getOrders(
        order: Int
    ) : OrderListData {
        var response:OrderListData? = null
        when(order){
            0-> {
                response = apiRequest { api.currentOrderList(
                    PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            1-> {
                response = apiRequest { api.pendingOrderList(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
        }

        return response!!
    }

    suspend fun setOrder(
        setParcel: SetParcel
    ) : SetParcelResponse {
        return apiRequest { api.setOrder(PersistentUser.getInstance().getAccessToken(
            appContext
        ), setParcel)}
    }
}