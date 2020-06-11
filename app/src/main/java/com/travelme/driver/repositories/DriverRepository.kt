package com.travelme.driver.repositories

import com.travelme.driver.models.Driver
import com.travelme.driver.utilities.WrappedResponse
import com.travelme.driver.webservices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.Error

class DriverRepository (private val api : ApiService){
    fun login(email : String, password : String, result : (String? , Error?)-> Unit){
        api.login(email, password).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) = result(null, Error(t.message))

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.status!!){
                        result(body.data!!.token, null)
                    }else{
                        result(null, Error(body.message))
                    }
                }else{
                    result(null, Error(response.message()))
                }
            }

        })
    }
}