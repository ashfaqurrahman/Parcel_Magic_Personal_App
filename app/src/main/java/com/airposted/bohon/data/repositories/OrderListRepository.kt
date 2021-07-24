package com.airposted.bohon.data.repositories

import android.content.Context
import android.media.Rating
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bohon.data.network.MyApi
import com.airposted.bohon.data.network.SafeApiRequest
import com.airposted.bohon.data.network.responses.SetParcelResponse
import com.airposted.bohon.model.*
import com.airposted.bohon.model.rating.RateDeliveryMan

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
            4-> {
                response = apiRequest { api.collectedOrderListExpress(
                    PersistentUser.getInstance().getAccessToken(
                        appContext
                    ))}
            }
            5-> {
                response = apiRequest { api.collectedOrderListQuick(
                    PersistentUser.getInstance().getAccessToken(
                        appContext
                    ))}
            }
            6-> {
                response = apiRequest { api.canceledOrderListExpress(
                    PersistentUser.getInstance().getAccessToken(
                        appContext
                    ))}
            }
            7-> {
                response = apiRequest { api.canceledOrderListQuick(
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

    suspend fun rating(rating: Int, id: Int, invoice: String): RateDeliveryMan {
        return apiRequest { api.rating(
            PersistentUser.getInstance().getAccessToken(
                appContext
            ), rating, id, invoice) }
    }
}