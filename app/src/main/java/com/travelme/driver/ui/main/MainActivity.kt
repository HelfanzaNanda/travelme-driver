package com.travelme.driver.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coil.api.load
import com.travelme.driver.R
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Driver
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.ui.maps.MapsActivity
import com.travelme.driver.ui.profile.ProfileActivity
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.txt_date
import kotlinx.android.synthetic.main.activity_main.txt_hour
import kotlinx.android.synthetic.main.activity_main.txt_total_user
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object{ var navStatus = -1 }
    private var fragment : Fragment? = null

    private val mainViewModel : MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        card_maps.setOnClickListener { startActivity(Intent(this, MapsActivity::class.java)) }
        card_profile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }

        observer()
        observe()
    }

    private fun observer() = mainViewModel.listenToState().observer(this, Observer { handleUiState(it) })

    private fun observe() {
        mainViewModel.listenToCurrentUser().observe(this, Observer { handleCurrentUser(it) })
        mainViewModel.listenToFetchShedulle().observe(this, Observer { handleShedulle(it) })
    }

    @SuppressLint("SetTextI18n")
    private fun handleShedulle(it: OrderForSchedulle) {
        if (!it.is_order){
            linear_schedulle.gone()
            linear_dont_schedulle.visible()
        }else{
            linear_dont_schedulle.gone()
            linear_schedulle.visible()
            txt_date.text = "${it.date}"
            txt_hour.text = "${it.hour} WIB"
            txt_departure.text = "${it.departure!!.from} -> ${it.departure!!.destination}"
            txt_total_user.text = "${it.total_user} Orang"
        }
    }

    private fun handleCurrentUser(it: Driver) {
        if (!it.avatar.isNullOrEmpty()){
            img_user.load(it.avatar)
        }
        txt_name.text = it.name
        txt_email.text = it.email
    }

    private fun handleUiState(it: MainState) {
        when(it){
            is MainState.Loading -> handleLoading(it.state)
            is MainState.ShowToast -> toast(it.message)
        }
    }

    private fun handleLoading(state: Boolean) = if (state) loading.visible() else loading.gone()

//    /*private fun isLoggedIn(){
//        if(Constants.getToken(this@MainActivity).equals("UNDEFINED")){
//            startActivity(Intent(this, LoginActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }).also { finish() }
//        }
//    }*/

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onResume() {
        super.onResume()
        mainViewModel.fetchSchedulle(Constants.getToken(this@MainActivity))
        mainViewModel.getCurrentUser(Constants.getToken(this@MainActivity))
    }
}
