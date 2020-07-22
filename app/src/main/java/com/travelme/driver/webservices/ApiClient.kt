package com.travelme.driver.webservices

import com.travelme.driver.models.Driver
import com.travelme.driver.models.Order
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.utilities.Constants
import com.travelme.driver.utilities.WrappedListResponse
import com.travelme.driver.utilities.WrappedResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

class ApiClient {
    companion object {
        private var retrofit: Retrofit? = null

        private val opt = OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
        }.build()

        private fun getClient(): Retrofit {
            return if (retrofit == null) {
                retrofit = Retrofit.Builder().apply {
                    client(opt)
                    baseUrl(Constants.END_POINT)
                    addConverterFactory(GsonConverterFactory.create())
                }.build()
                retrofit!!
            } else {
                retrofit!!
            }
        }

        fun instance() = getClient().create(ApiService::class.java)
    }
}

interface ApiService{

    @GET("order/driver")
    fun getOrders(
        @Header("Authorization") token : String
    ) : Call<WrappedListResponse<Order>>

    @GET("order/driver/show")
    fun getOrder(
        @Header("Authorization") token : String
    ) : Call<WrappedResponse<OrderForSchedulle>>
    
    @GET("order/{id}/arrived")
    fun arrived(
        @Header("Authorization") token : String,
        @Path("id") id : Int
    ) : Call<WrappedResponse<Order>>

    @GET("order/{id}/done")
    fun done(
        @Header("Authorization") token : String,
        @Path("id") id : Int
    ) : Call<WrappedResponse<Order>>

    @FormUrlEncoded
    @POST("driver/login")
    fun login(
        @Field("email") email : String,
        @Field("password") password : String
    ) : Call<WrappedResponse<Driver>>

    @GET("driver/profile")
    fun profile(
        @Header("Authorization") token : String
    ) : Call<WrappedResponse<Driver>>

    @GET("driver/location/{location}")
    fun setLocation(
        @Header("Authorization") token : String,
        @Path("location") loc : String
    ) : Call<WrappedResponse<Driver>>

    @FormUrlEncoded
    @POST("driver/profile/update")
    fun updateProfile(
        @Header("Authorization") token : String,
        @Field("name") name : String,
        @Field("password") pass : String
    ) : Call<WrappedResponse<Driver>>

    @Multipart
    @POST("driver/profile/update/photo")
    fun updatePhotoProfile(
        @Header("Authorization") token : String,
        @Part image : MultipartBody.Part
    ) :Call<WrappedResponse<Driver>>
}