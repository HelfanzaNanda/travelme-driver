package com.travelme.driver.ui.update_profile

import androidx.lifecycle.ViewModel
import com.travelme.driver.models.Driver
import com.travelme.driver.repositories.DriverRepository
import com.travelme.driver.utilities.Constants
import com.travelme.driver.utilities.SingleLiveEvent
import com.travelme.driver.utilities.SingleResponse

class UpdateProfilViewModel(private val driverRepository: DriverRepository) : ViewModel() {
    private val state: SingleLiveEvent<UpdateProfilState> = SingleLiveEvent()
    private fun setLoading() { state.value = UpdateProfilState.Loading(true) }
    private fun hideLoading() { state.value = UpdateProfilState.Loading(false) }
    private fun toast(message: String) { state.value = UpdateProfilState.ShowToast(message) }
    private fun success() { state.value = UpdateProfilState.Success }

    fun updateProfile(token: String, name : String, pass : String, pathImage: String) {
        setLoading()
        if (name.isEmpty()){
            updatePhotoProfile(token, pathImage)
        }else {
            driverRepository.updateProfil(token, name, pass, object : SingleResponse<Driver>{
                override fun onSuccess(data: Driver?) {
                    hideLoading()
                    if (pathImage.isNotEmpty()) {
                        updatePhotoProfile(token, pathImage)
                    } else {
                        success()
                    }
                }

                override fun onFailure(err: Error?) {
                    err?.let { toast(it.message.toString()) }
                }
            })
        }
    }

    private fun updatePhotoProfile(token: String, pathImage: String) {
        driverRepository.updatePhotoProfil(token, pathImage, object : SingleResponse<Driver> {
            override fun onSuccess(data: Driver?) {
                hideLoading()
                data?.let { success() }
            }

            override fun onFailure(err: Error?) {
                hideLoading()
                err?.let { toast(it.message.toString()) }
            }
        })
    }

    fun listenToState() = state
}

sealed class UpdateProfilState {
    data class Loading(var state: Boolean = false) : UpdateProfilState()
    data class ShowToast(var message: String) : UpdateProfilState()
    object Success : UpdateProfilState()
    data class Validate(
        var name : String? = null,
        var pass: String? = null
    ) : UpdateProfilState()
    object Reset : UpdateProfilState()
}