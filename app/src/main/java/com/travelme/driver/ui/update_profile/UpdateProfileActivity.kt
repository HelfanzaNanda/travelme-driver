package com.travelme.driver.ui.update_profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import coil.api.load
import com.fxn.pix.Pix
import com.travelme.driver.R
import com.travelme.driver.models.Driver
import com.travelme.driver.utilities.Constants

import kotlinx.android.synthetic.main.activity_update_profile.*
import kotlinx.android.synthetic.main.content_update_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class UpdateProfileActivity : AppCompatActivity() {

    private val updateProfilViewModel : UpdateProfilViewModel by viewModel()
    private var imgUrl = ""
    private val REQ_CODE_PIX = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        //setSupportActionBar(toolbar)
        setUi()
        btn_add_image.setOnClickListener { Pix.start(this, REQ_CODE_PIX) }
        observer()
        updateProfil()
    }

    private fun setUi(){
        getPassedDriver()?.let {
            img_user.load(it.avatar)
            et_name.setText(it.name)
            et_email.setText(it.email)
            et_telp.setText(it.telp)
        }
    }

    private fun updateProfil() {
        fab_update.setOnClickListener {
            val token = Constants.getToken(this@UpdateProfileActivity)
            val name = et_name.text.toString().trim()
            val password = et_password.text.toString().trim()
            if (validate(name)){
                updateProfilViewModel.updateProfile(token, name, password, imgUrl)
            }
        }
    }

    private fun validate(name : String) : Boolean{
        if(name.isEmpty()){
            toast("nama tidak boleh kosong")
            return false
        }
        return true
    }

    private fun observer() {
        updateProfilViewModel.listenToState().observer(this, Observer { handleUiState(it) })
    }

    private fun handleUiState(it: UpdateProfilState) {
        when(it){
            is UpdateProfilState.Loading -> handleLoading(it.state)
            is UpdateProfilState.ShowToast -> toast(it.message)
            is UpdateProfilState.Success -> handleSuccess()
        }
    }

    private fun handleLoading(state: Boolean) {
        fab_update.isEnabled = !state
    }

    private fun handleSuccess() {
        finish()
        toast("berhasil mengupdate profile")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CODE_PIX && resultCode == Activity.RESULT_OK && data != null){
            val selectedImageUri = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
            selectedImageUri?.let {
                img_user.load(File(it[0]))
                imgUrl = it[0]
            }
        }
    }

    private fun toast(message : String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun getPassedDriver() : Driver? = intent.getParcelableExtra("DRIVER")
}
