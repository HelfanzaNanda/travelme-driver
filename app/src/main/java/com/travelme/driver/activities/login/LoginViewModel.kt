package com.travelme.driver.activities.login

import androidx.lifecycle.ViewModel
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.utilities.Constants
import com.travelme.driver.utilities.SingleLiveEvent

class LoginViewModel (private val driverRepository: DriverRepository) : ViewModel(){
    private val state : SingleLiveEvent<LoginState>  = SingleLiveEvent()

    private fun setLoading() { state.value = LoginState.IsLoading(true) }
    private fun hideLoading() { state.value = LoginState.IsLoading(false) }
    private fun toast(message : String) { state.value = LoginState.ShowToast(message) }
    private fun successLogin(token: String) { state.value = LoginState.SuccessLogin(token) }
    private fun reset() { state.value = LoginState.Reset }

    fun login(email : String, password : String){
        setLoading()
        driverRepository.login(email, password){result, error->
            hideLoading()
            error?.let { it.message?.let { message->toast(message) } }
            result?.let { successLogin(it) }
        }
    }

    fun validate(email: String, password: String) : Boolean{
        reset()
        if (email.isEmpty() || password.isEmpty()){
            state.value = LoginState.ShowToast("mohon isi semua form")
            return false
        }
        if (!Constants.isValidEmail(email)){
            state.value = LoginState.Validate(email = "email tidak valid")
            return false
        }
        if (!Constants.isValidPassword(password)){
            state.value = LoginState.Validate(password = "password tidak valid")
            return false
        }

        return true
    }

    fun listenToState() = state

}

sealed class LoginState{
    data class IsLoading(var state : Boolean = false) : LoginState()
    data class ShowToast(var message : String) : LoginState()
    data class SuccessLogin(var token : String) : LoginState()
    data class Validate(
        var email: String? = null,
        var password: String? = null
    ) : LoginState()
    object Reset : LoginState()
}