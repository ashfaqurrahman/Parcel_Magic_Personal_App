package com.airposted.bitoronbd.data.network

import com.airposted.bitoronbd.data.network.responses.AuthResponse
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.data.network.responses.SettingResponse
import com.airposted.bitoronbd.model.*
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

    @POST("personal/currentorderlistquick")
    suspend fun currentOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/currentorderlistexpress")
    suspend fun currentOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/historyorderlistexpress")
    suspend fun historyOrderListExpress(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @POST("personal/historyorderlistquick")
    suspend fun historyOrderListQuick(
        @Header("Authorization") header: String
    ) : Response<OrderList>

    @GET
    suspend fun getPlacesNameList(@Url url: String): Response<SearchLocation>

    @GET
    suspend fun getDirectionsList(@Url url: String): Response<GoogleMapDTO>

    @GET("app_settings")
    suspend fun getSetting(): Response<SettingResponse>

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

