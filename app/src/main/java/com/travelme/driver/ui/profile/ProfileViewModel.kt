package com.travelme.driver.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Driver
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.repositories.OrderRepository
import com.travelme.driver.utilities.SingleLiveEvent
import com.travelme.driver.utilities.SingleResponse

class ProfileViewModel (private val driverRepository: DriverRepository,
                        private val orderRepository: OrderRepository) : ViewModel(){
    private val driver = MutableLiveData<Driver>()
    private val state : SingleLiveEvent<ProfileState> = SingleLiveEvent()
    private val orderForSchedulle  = MutableLiveData<OrderForSchedulle>()

    private fun setLoading() { state.value = ProfileState.IsLoading(true) }
    private fun hideLoading() { state.value = ProfileState.IsLoading(false) }
    private fun toast(message: String) { state.value = ProfileState.ShowToast(message) }

    fun profile(token : String){
        driverRepository.profile(token, object : SingleResponse<Driver> {
            override fun onSuccess(data: Driver?) {
                hideLoading()
                data?.let { driver.postValue(it) }
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
                err?.let { toast(it.message.toString()) }
            }
        })
    }

    fun setLocation(token : String, loc : String){
        setLoading()
        driverRepository.setLocation(token, loc, object : SingleResponse<Driver>{
            override fun onSuccess(data: Driver?) {
                hideLoading()
                data?.let { state.value = ProfileState.Success }
            }

            override fun onFailure(err: Error?) {
                hideLoading()
                err?.let { it.message.toString() }
            }

        })
    }

    fun listenToState() = state
    fun listenToDriver() = driver
    fun listenToFetchShedulle() = orderForSchedulle
}

sealed class ProfileState{
    data class IsLoading(var state : Boolean = false) : ProfileState()
    data class ShowToast(var message : String) : ProfileState()
    object Success : ProfileState()
}