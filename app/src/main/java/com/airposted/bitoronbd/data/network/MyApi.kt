package com.airposted.bitoronbd.data.network

import com.airposted.bitoronbd.data.network.responses.AuthResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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

    /*@GET("quotes")
    suspend fun getQuotes() : Response<QuotesResponse>*/

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

