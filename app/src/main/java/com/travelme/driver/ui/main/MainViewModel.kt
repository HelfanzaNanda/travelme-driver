package com.travelme.driver.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Driver
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.repositories.OrderRepository
import com.travelme.driver.utilities.SingleLiveEvent
import com.travelme.driver.utilities.SingleResponse

class MainViewModel (private val driverRepository: DriverRepository,
                     private val orderRepository: OrderRepository) : ViewModel(){
    private val state : SingleLiveEvent<MainState> = SingleLiveEvent()
    private val currentUser = MutableLiveData<Driver>()
    private val orderForSchedulle  = MutableLiveData<OrderForSchedulle>()

    private fun setLoading(){ state.value = MainState.Loading(true) }
    private fun hideLoading(){ state.value = MainState.Loading(false) }
    private fun toast(message: String){ state.value = MainState.ShowToast(message) }

    fun getCurrentUser(token : String){
        setLoading()
        driverRepository.profile(token, object : SingleResponse<Driver>{
            override fun onSuccess(data: Driver?) {
                hideLoading()
                data?.let { currentUser.postValue(it) }
            }

            override fun onFailure(err: Error?) {
                hideLoading()
                err?.let { toast(err.message.toString()) }
            }
        })
    }

    fun fetchSchedulle(token: String){
        setLoading()
        orderRepository.fetchSchedulle(token, object : SingleResponse<OrderForSchedulle>{
            override fun onSuccess(data: OrderForSchedulle?) {
                hideLoading()
                data?.let { orderForSchedulle.postValue(it) }
            }

            override fun onFailure(err: Error?) {
                hideLoading()
                err?.let { toast(err.message.toString()) }
            }
        })
    }

    fun listenToFetchShedulle() = orderForSchedulle
    fun listenToState() = state
    fun listenToCurrentUser() = currentUser

}

sealed class MainState{
    data class Loading(var state : Boolean = false) : MainState()
    data class ShowToast(var message : String) : MainState()
}