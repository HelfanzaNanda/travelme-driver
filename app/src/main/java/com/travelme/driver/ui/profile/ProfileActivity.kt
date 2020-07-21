package com.travelme.driver.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import coil.api.load
import com.travelme.driver.R
import com.travelme.driver.activities.login.LoginActivity
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Driver
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.ui.update_profile.UpdateProfileActivity
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : AppCompatActivity() {

    private val profileViewModel : ProfileViewModel by viewModel()
    private lateinit var loc : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()
        profileViewModel.listenToState().observer(this, Observer { handleUI(it) })
        profileViewModel.listenToDriver().observe(this, Observer { handleDriver(it) })

        logout()

    }

    private fun handleSchedulle(it: OrderForSchedulle) {
        if (!it.is_order){
            btn_domicile.isEnabled = true
            btn_domicile.setOnClickListener {popup("belum memiliki jadwal")}
        }else{
            if (it.departure!!.from.equals(loc)){
                btn_domicile.setOnClickListener {
                    profileViewModel.goOff(Constants.getToken(this@ProfileActivity))
                }
            }
        }
        //println(it.departure!!.from!!)
        //loc = it.departure!!.from!!
        //btn_domicile.isEnabled = true
    }

    private fun popup(message: String){
        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton("ya"){dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    private fun handleUI(it : ProfileState){
        when(it){
            is ProfileState.IsLoading -> {
                if (it.state){
                    pb_profile.visible()
                }else{
                    pb_profile.gone()
                }
            }
            is ProfileState.ShowToast -> toast(it.message)
        }
    }

    private fun logout(){
        btn_logout.setOnClickListener {
            Constants.clearToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            this.finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleDriver(it : Driver) {
        profile_image.load(it.avatar)
        txt_name.text = it.name
        txt_email.text = it.email
        txt_telp.text = it.telp
        txt_domicile.text = "saya berada di ${it.location}"
        loc = it.location!!
        profileViewModel.listenToFetchShedulle().observe(this, Observer { handleSchedulle(it) })

        btn_edit_profile.setOnClickListener { _-> startActivity(Intent(this, UpdateProfileActivity::class.java).apply {
            putExtra("DRIVER", it)
        }) }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.profile(Constants.getToken(this@ProfileActivity))
        profileViewModel.fetchSchedulle(Constants.getToken(this@ProfileActivity))
    }

    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
}
