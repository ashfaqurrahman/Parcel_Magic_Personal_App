package com.airposted.bitoronbd.data.repositories

import android.content.Context
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.SetParcel
import com.airposted.bitoronbd.model.OrderList
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.model.GoogleMapDTO
import com.airposted.bitoronbd.model.StatusChangeModel

class OrderListRepository(context: Context, private val api: MyApi) : SafeApiRequest() {
    private val appContext = context.applicationContext

    suspend fun getOrders(
        order: Int
    ) : OrderList {
        var response:OrderList? = null
        when(order){
            0-> {
                response = apiRequest { api.currentOrderListExpress(
                    PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            1-> {
                response = apiRequest { api.currentOrderListQuick(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            2-> {
                response = apiRequest { api.historyOrderListExpress(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            3-> {
                response = apiRequest { api.historyOrderListQuick(
                    PersistentUser.getInstance().getAccessToken(
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

    suspend fun changeStatus(invoice: String, status: Int): StatusChangeModel {
        return apiRequest {
            api.changeStatus(
                PersistentUser.getInstance().getAccessToken(appContext), invoice, status
            )
        }
    }

    suspend fun directionSearch(url: String): GoogleMapDTO {
        return apiRequest { api.getDirectionsList(url)}
    }
}