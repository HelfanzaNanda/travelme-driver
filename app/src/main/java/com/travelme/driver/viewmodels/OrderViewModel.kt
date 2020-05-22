package com.travelme.driver.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.SingleLiveEvent
import com.travelme.driver.utilities.WrappedListResponse
import com.travelme.driver.webservices.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderViewModel : ViewModel(){
    private var orders = MutableLiveData<List<Order>>()
    private var state : SingleLiveEvent<OrderState> = SingleLiveEvent()
    private var api = ApiClient.instance()


    fun getOrder(token : String){
        state.value = OrderState.IsLoading(false)
        api.getOrder(token).enqueue(object : Callback<WrappedListResponse<Order>>{
            override fun onFailure(call: Call<WrappedListResponse<Order>>, t: Throwable) {
                println("OnFailure : "+t.message)
            }

            override fun onResponse(call: Call<WrappedListResponse<Order>>, response: Response<WrappedListResponse<Order>>) {
                if (response.isSuccessful){
                    val body = response.body()
                    if (body?.status!!){
                        val data  = body.data
                        orders.postValue(data)
                    }else{
                        state.value = OrderState.ShowToast("tidak dapat menampilkan data")
                    }
                }else{
                    state.value = OrderState.ShowToast("tidak dapat menampilkan")
                }
            }
        })
    }

    fun getState() = state
    fun getOrders() = orders

}

sealed class OrderState{
    data class IsLoading (var state : Boolean = false) : OrderState()
    data class ShowToast(var message : String) : OrderState()
}