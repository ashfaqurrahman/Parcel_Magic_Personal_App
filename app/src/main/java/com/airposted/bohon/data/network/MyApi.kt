package com.airposted.bohon.data.network

import com.airposted.bohon.data.network.responses.SetParcelResponse
import com.airposted.bohon.data.network.responses.SettingResponse
import com.airposted.bohon.model.*
import com.airposted.bohon.model.auth.AuthResponse
import com.airposted.bohon.model.coupon.CheckCouponModel
import com.airposted.bohon.model.coupon.UserCoupon
import com.airposted.bohon.model.rating.RateDeliveryMan
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface MyApi {

    @FormUrlEncoded
    @POST("personal-phonenumber-check")
    suspend fun numberCheck(
        @Field("phone") email: String
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("send-otp-message")
    suspend fun sendOTP(
        @Field("phone_number") phone: String
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("register-personal")
    suspend fun userSignup(
        @Field("username") name: String,
        @Field("phone") email: String
    ) : Response<AuthResponse>

    @Multipart
    @POST("register-personal")
    suspend fun userSignupWithPhoto(
        @Part("username") name: RequestBody,
        @Part("phone") email: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("image") requestBody: RequestBody
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("personal/user-update")
    suspend fun userNameUpdate(
        @Header("Authorization") header: String,
        @Field("username") name: String
    ) : Response<AuthResponse>

    @Multipart
    @POST("personal/user-update")
    suspend fun userImageUpdate(
        @Header("Authorization") header: String,
        @Part file: MultipartBody.Part,
        @Part("image") requestBody: RequestBody
    ) : Response<AuthResponse>

    @POST("personal/set-parcel")
    suspend fun setOrder(
        @Header("Authorization") header: String,
        @Body setParcel: SetParcel
    ): Response<SetParcelResponse>

    @POST("personal/current-order-list-instant")
    suspend fun currentOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/current-order-list-express")
    suspend fun currentOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @FormUrlEncoded
    @POST("delivery/order-status-change")
    suspend fun changeStatus(
        @Header("Authorization") header: String,
        @Field("invoice_no") invoice: String,
        @Field("current_status") status: Int
    ) : Response<StatusChangeModel>

    @POST("personal/complete-order-list-express")
    suspend fun historyOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/complete-order-list-instant")
    suspend fun historyOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/collected-order-list-quick")
    suspend fun collectedOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/collected-order-list-express")
    suspend fun collectedOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/cancel-order-list-quick")
    suspend fun canceledOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/cancel-order-list-express")
    suspend fun canceledOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @GET
    suspend fun getPlacesNameList(@Url url: String): Response<SearchLocation>

    @GET
    suspend fun locationDetails(@Url url: String): Response<LocationDetails>

    @GET
    suspend fun getDirectionsList(@Url url: String): Response<GoogleMapDTO>

    @GET("app-settings")
    suspend fun getSetting(): Response<SettingResponse>

    @FormUrlEncoded
    @POST("personal/add-fcm-token")
    suspend fun saveFcmToken(
        @Header("Authorization") header: String,
        @Field("fcm_token") fcm_token: String,
    ): Response<SetParcelResponse>

    @GET("personal/delete-fcm-token")
    suspend fun deleteFcmToken(
        @Header("Authorization") header: String
    ): Response<SetParcelResponse>

    @GET("personal/user-based-coupons")
    suspend fun getUserBasedCoupons(
        @Header("Authorization") header: String
    ): Response<UserCoupon>

    @FormUrlEncoded
    @POST("personal/set-rating")
    suspend fun rating(
        @Header("Authorization") header: String,
        @Field("rating") rating: Int,
        @Field("logisticId") logisticId: Int,
        @Field("invoice") invoice: String
    ) : Response<RateDeliveryMan>

    @FormUrlEncoded
    @POST("personal/check-coupon")
    suspend fun checkCoupon(
        @Header("Authorization") header: String,
        @Field("coupon_text") couponText: String
    ) : Response<CheckCouponModel>

    companion object{
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ) : MyApi{

            val okkHttpclient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)
                .baseUrl("https://api.parcelmagic.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyApi::class.java)
        }
    }

}

