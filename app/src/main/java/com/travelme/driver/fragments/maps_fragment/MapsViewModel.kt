package com.travelme.driver.fragments.maps_fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Order
import com.travelme.driver.repositories.OrderRepository
import com.travelme.driver.utilities.SingleLiveEvent

class MapsViewModel (private val orderRepository: OrderRepository) : ViewModel(){

    private val state : SingleLiveEvent<MapsState> = SingleLiveEvent()
    private val orders = MutableLiveData<List<Order>>()

    private fun toast(message: String) { state.value = MapsState.ShowToast(message) }
    private fun setLoading() { state.value = MapsState.IsLoading(true) }
    private fun hideLoading() { state.value = MapsState.IsLoading(false) }
    private fun successArrived() { state.value = MapsState.SuccessArrived }
    private fun successDone() { state.value = MapsState.SuccessDone }

    fun getOrders(token : String){
        setLoading()
        orderRepository.getOrders(token){resultOrder, error->
            hideLoading()
            error?.let { it.message?.let { message-> toast(message) }}
            resultOrder?.let { orders.postValue(it) }
        }
    }



    fun arrived(token: String, id : String){
        setLoading()
        orderRepository.arrived(token, id){resutlBool, error->
            hideLoading()
            error?.let { it.message?.let { message-> toast(message) }}
            if (resutlBool){
                successArrived()
            }
        }
    }

    fun done(token: String, id : String){
        setLoading()
        orderRepository.done(token, id){resultBool, error->
            hideLoading()
            error?.let { it.message?.let { message-> toast(message) }}
            if (resultBool){
                successDone()
            }
        }
    }

    fun listenToState() = state
    fun listenToOrders() = orders

}

sealed class MapsState{
    data class IsLoading(var state : Boolean = false) : MapsState()
    data class ShowToast(var message : String) : MapsState()
    object SuccessArrived : MapsState()
    object SuccessDone : MapsState()
}