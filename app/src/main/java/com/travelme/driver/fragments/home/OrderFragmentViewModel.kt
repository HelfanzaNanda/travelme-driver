package com.travelme.driver.fragments.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Order
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.repositories.OrderRepository
import com.travelme.driver.utilities.SingleLiveEvent

class OrderFragmentViewModel (private val orderRepository: OrderRepository) : ViewModel(){

    private var orders = MutableLiveData<List<Order>>()
    private var order = MutableLiveData<OrderForSchedulle>()
    private val state : SingleLiveEvent<OrderFragmentState> = SingleLiveEvent()

    private fun setLoading() { state.value = OrderFragmentState.IsLoading(true) }
    private fun hideLoading() { state.value = OrderFragmentState.IsLoading(false) }
    private fun toast(message: String) { state.value = OrderFragmentState.ShowToast(message) }

    fun getOrders(token : String){
        setLoading()
        orderRepository.getOrders(token){resultOrders, error->
            hideLoading()
            error?.let { it.message?.let { message-> toast(message) }}
            resultOrders?.let { orders.postValue(it) }
        }
    }

    fun getOrder(token : String){
        setLoading()
//        orderRepository.getOrder(token){resultOrder, error ->
//            hideLoading()
//            error?.let { it.message?.let { message->toast(message) } }
//            resultOrder?.let {
//                order.postValue(it)
//                println(it)
//            }
//        }
    }

    fun listenToOrders() = orders
    fun listenToOrder() = order
    fun listenToState() = state

}
sealed class OrderFragmentState{
    data class IsLoading(var state : Boolean = false) : OrderFragmentState()
    data class ShowToast(var message : String?) : OrderFragmentState()
}