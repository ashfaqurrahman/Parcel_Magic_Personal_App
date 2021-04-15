package com.airposted.bitoronbd.data.repositories

import android.content.Context
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.SetParcel
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.model.CurrentOrder

class OrderHistoryListRepository(context: Context, private val api: MyApi) : SafeApiRequest() {
    private val appContext = context.applicationContext

    suspend fun getOrders(
        order: Int
    ) : CurrentOrder {
        var response:CurrentOrder? = null
        when(order){
            0-> {
                response = apiRequest { api.currentOrderList(
                    PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            1-> {
                response = apiRequest { api.completeOrderList(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            2-> {
                response = apiRequest { api.cancelOrderList(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            3-> {
                response = apiRequest { api.currentOrderList(
                    PersistentUser.getInstance().getAccessToken(
                        appContext
                    ))}
            }
            4-> {
                response = apiRequest { api.completeOrderList(PersistentUser.getInstance().getAccessToken(
                    appContext
                ))}
            }
            5-> {
                response = apiRequest { api.cancelOrderList(PersistentUser.getInstance().getAccessToken(
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