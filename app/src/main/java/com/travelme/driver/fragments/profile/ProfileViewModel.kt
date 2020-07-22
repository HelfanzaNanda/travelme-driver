package com.travelme.driver.fragments.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Driver
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.utilities.SingleLiveEvent

class ProfileViewModel (private val driverRepository: DriverRepository) : ViewModel(){
    private val driver = MutableLiveData<Driver>()
    private val state : SingleLiveEvent<ProfileState> = SingleLiveEvent()

    private fun setLoading() { state.value = ProfileState.IsLoading(true) }
    private fun hideLoading() { state.value = ProfileState.IsLoading(false) }
    private fun toast(message: String) { state.value = ProfileState.ShowToast(message) }

    fun listenToState() = state
    fun listenToDriver() = driver
}

sealed class ProfileState{
    data class IsLoading(var state : Boolean = false) : ProfileState()
    data class ShowToast(var message : String) : ProfileState()
}