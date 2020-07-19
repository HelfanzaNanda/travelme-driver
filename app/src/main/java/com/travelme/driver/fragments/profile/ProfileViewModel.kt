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

    fun profile(token : String){
        setLoading()
        driverRepository.profile(token){resultDriver, error->
            hideLoading()
            error?.let { it.message?.let { message-> toast(message) }}
            resultDriver?.let { driver.postValue(it) }
        }
    }

    fun domicile(token: String){
        setLoading()
        driverRepository.domicile(token){resultDriver, error ->
            hideLoading()
            error?.let { it.message?.let { message->toast(message) } }
            resultDriver?.let { profile(token) }
        }
    }


    fun goOff(token: String){
        setLoading()
        driverRepository.goOff(token){resultDriver, error ->
            hideLoading()
            error?.let { it.message?.let { message->toast(message) } }
            resultDriver?.let { profile(token) }
        }
    }

    fun listenToState() = state
    fun listenToDriver() = driver
}

sealed class ProfileState{
    data class IsLoading(var state : Boolean = false) : ProfileState()
    data class ShowToast(var message : String) : ProfileState()
}