package com.travelme.driver.repositories

import com.travelme.driver.models.Driver
import com.travelme.driver.utilities.SingleResponse
import com.travelme.driver.utilities.WrappedResponse
import com.travelme.driver.webservices.ApiService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.Error

interface DriverContract{
    fun profile(token: String, listener : SingleResponse<Driver>)
    fun updateProfil(token: String, name : String, password: String, listener: SingleResponse<Driver>)
    fun updatePhotoProfil(token: String, imgUrl : String, listener: SingleResponse<Driver>)
}

class DriverRepository (private val api : ApiService) : DriverContract{
    override fun updatePhotoProfil(token: String, imgUrl: String, listener: SingleResponse<Driver>) {
        val file = File(imgUrl)
        val requestBodyForFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("avatar", file.name, requestBodyForFile)

        api.updatePhotoProfile(token, image).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) {
                listener.onFailure(Error(t.message))
            }

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                when{
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body?.status!!){
                            listener.onSuccess(body.data)
                        }
                    }
                    !response.isSuccessful -> listener.onFailure(Error(response.message()))
                }
            }

        })
    }

    override fun updateProfil(token: String, name: String, password: String, listener: SingleResponse<Driver>) {
        api.updateProfile(token, name, password).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) {
                listener.onFailure(Error(t.message))
            }

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                when{
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body?.status!!){
                            listener.onSuccess(body.data)
                        }
                    }
                    !response.isSuccessful -> listener.onFailure(Error(response.message()))
                }
            }

        })
    }

    override fun profile(token: String, listener: SingleResponse<Driver>) {
        api.profile(token).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) {
                listener.onFailure(Error(t.message))
            }

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                when{
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body?.status!!){
                            listener.onSuccess(body.data)
                        }else{
                            listener.onFailure(Error(body.message))
                        }
                    }
                    !response.isSuccessful -> listener.onFailure(Error(response.message()))
                }
            }

        })
    }

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

    fun domicile(token : String, result: (Driver?, Error?) -> Unit){
        api.domicile(token).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) {
                result(null, Error(t.message))
            }

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.status!!){
                        result(body.data, null)
                    }else{
                        result(null, Error(body.message))
                    }
                }else{
                    result(null, Error(response.message()))
                }
            }

        })
    }

    fun goOff(token : String, result: (Driver?, Error?) -> Unit){
        api.goOff(token).enqueue(object : Callback<WrappedResponse<Driver>>{
            override fun onFailure(call: Call<WrappedResponse<Driver>>, t: Throwable) {
                result(null, Error(t.message))
            }

            override fun onResponse(call: Call<WrappedResponse<Driver>>, response: Response<WrappedResponse<Driver>>) {
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.status!!){
                        result(body.data, null)
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