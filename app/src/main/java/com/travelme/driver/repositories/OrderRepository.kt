package com.travelme.driver.repositories

import com.travelme.driver.models.Order
import com.travelme.driver.utilities.WrappedListResponse
import com.travelme.driver.webservices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderRepository (private val api : ApiService){
    fun getOrders(token: String, result : (List<Order>?, Error?)-> Unit){
        println(token)
        api.getOrders(token).enqueue(object : Callback<WrappedListResponse<Order>>{
            override fun onFailure(call: Call<WrappedListResponse<Order>>, t: Throwable) {
                result(null, Error(t.message))
                println(t.message)
            }

            override fun onResponse(call: Call<WrappedListResponse<Order>>, response: Response<WrappedListResponse<Order>>) {
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.status!!){
                        val data = body.data
                        result(data, null)
                    }else{
                        result(null, Error())
                    }
                }else{
                    result(null, Error(response.message()))
                }
            }

        })
    }
}