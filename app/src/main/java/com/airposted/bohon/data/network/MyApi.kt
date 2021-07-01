package com.airposted.bohon.data.network

import com.airposted.bohon.data.network.responses.AuthResponse
import com.airposted.bohon.data.network.responses.SetParcelResponse
import com.airposted.bohon.data.network.responses.SettingResponse
import com.airposted.bohon.model.*
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface MyApi {

    @FormUrlEncoded
    @POST("phonenumbercheck")
    suspend fun numberCheck(
        @Field("phone") email: String
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("send_otp_message")
    suspend fun sendOTP(
        @Field("phone_number") phone: String
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("register_personal")
    suspend fun userSignup(
        @Field("username") name: String,
        @Field("phone") email: String
    ) : Response<AuthResponse>

    @Multipart
    @POST("register_personal")
    suspend fun userSignupWithPhoto(
        @Part("username") name: RequestBody,
        @Part("phone") email: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("image") requestBody: RequestBody
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("personal/userupdate")
    suspend fun userNameUpdate(
        @Header("Authorization") header: String,
        @Field("username") name: String
    ) : Response<AuthResponse>

    @Multipart
    @POST("personal/userupdate")
    suspend fun userImageUpdate(
        @Header("Authorization") header: String,
        @Part file: MultipartBody.Part,
        @Part("image") requestBody: RequestBody
    ) : Response<AuthResponse>

    @POST("personal/set_parcel")
    suspend fun setOrder(
        @Header("Authorization") header: String,
        @Body setParcel: SetParcel
    ): Response<SetParcelResponse>

    @POST("personal/currentorderlistinstant")
    suspend fun currentOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/currentorderlistexpress")
    suspend fun currentOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @FormUrlEncoded
    @POST("delivery/orderstatuschange")
    suspend fun changeStatus(
        @Header("Authorization") header: String,
        @Field("invoice_no") invoice: String,
        @Field("current_status") status: Int
    ) : Response<StatusChangeModel>

    @POST("personal/completeorderlistexpress")
    suspend fun historyOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/completeorderlistinstant")
    suspend fun historyOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/collectedorderlistquick")
    suspend fun collectedOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/collectedorderlistexpress")
    suspend fun collectedOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/cancelorderlistquick")
    suspend fun canceledOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/cancelorderlistexpress")
    suspend fun canceledOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @GET
    suspend fun getPlacesNameList(@Url url: String): Response<SearchLocation>

    @GET
    suspend fun locationDetails(@Url url: String): Response<LocationDetails>

    @GET
    suspend fun getDirectionsList(@Url url: String): Response<GoogleMapDTO>

    @GET("app_settings")
    suspend fun getSetting(): Response<SettingResponse>

    @FormUrlEncoded
    @POST("personal/addfcmtoken")
    suspend fun saveFcmToken(
        @Header("Authorization") header: String,
        @Field("fcm_token") fcm_token: String,
    ): Response<SetParcelResponse>

    @GET("personal/deletefcmtoken")
    suspend fun deleteFcmToken(
        @Header("Authorization") header: String
    ): Response<SetParcelResponse>

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
                .baseUrl("https://parcel.airposted.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyApi::class.java)
        }
    }

}

