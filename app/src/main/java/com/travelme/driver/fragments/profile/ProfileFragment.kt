package com.travelme.driver.fragments.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.travelme.driver.R
import com.travelme.driver.activities.login.LoginActivity
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Driver
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment(R.layout.fragment_profile){

    private val profileViewModel : ProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.listenToState().observer(viewLifecycleOwner, Observer { handleUI(it) })
        profileViewModel.listenToDriver().observe(viewLifecycleOwner, Observer { handleDriver(it) })
        logout()
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
            Constants.clearToken(requireActivity())
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleDriver(it : Driver){
        txt_name.text = it.name
        txt_email.text = it.email
        txt_telp.text = it.telp
        txt_domicile.text = "saya berada di ${it.location}"

//        if (it.location == ){
//
//            btn_domicile.text = "saya mau berangkat"
//            btn_domicile.setOnClickListener {
//                profileViewModel.goOff(Constants.getToken(requireActivity()))
//            }
//        }else{
//            txt_domicile.text = "saya tidak berada di ${it.owner.domicile}"
//            btn_domicile.text = "saya berada di ${it.owner.domicile}"
//            btn_domicile.setOnClickListener {
//                profileViewModel.domicile(Constants.getToken(requireActivity()))
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.profile(Constants.getToken(requireActivity()))
    }

    private fun toast(message: String) { Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show() }
}