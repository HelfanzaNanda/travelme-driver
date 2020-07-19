package com.travelme.driver.fragments.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelme.driver.R
import com.travelme.driver.adapters.OrderAdapter
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Order
import com.travelme.driver.models.OrderForSchedulle
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home){
    private val orderFragmentViewModel : OrderFragmentViewModel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderFragmentViewModel.listenToState().observer(viewLifecycleOwner, Observer { handleUI(it) })
        orderFragmentViewModel.listenToOrder().observe(viewLifecycleOwner, Observer { handleOrder(it) })
    }

    @SuppressLint("SetTextI18n")
    private fun handleOrder(it : OrderForSchedulle){
        txt_date.text = "${it.date}"
        txt_hour.text = "${it.hour} WIB"
        txt_departure.text = "${it.departure.from} -> ${it.departure.destination}"
        txt_total_user.text = "${it.total_user} Orang"
    }

    private fun handleUI(it : OrderFragmentState){
        when(it){
            is OrderFragmentState.IsLoading -> {
                if (it.state){
                    pb_home.visible()
                }else{
                    pb_home.gone()
                }
            }
            is OrderFragmentState.ShowToast -> toast(it.message!!)
        }
    }

    private fun toast(message : String) { Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show() }

    override fun onResume() {
        super.onResume()
        orderFragmentViewModel.getOrder(Constants.getToken(requireActivity()))
    }
}