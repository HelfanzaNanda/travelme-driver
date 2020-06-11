package com.travelme.driver.fragments.home_fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelme.driver.R
import com.travelme.driver.adapters.OrderAdapter
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home){
    private val orderFragmentViewModel : OrderFragmentViewModel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_home.apply {
            adapter = OrderAdapter(mutableListOf(), requireActivity())
            layoutManager = LinearLayoutManager(requireActivity())
        }
        orderFragmentViewModel.listenToState().observer(viewLifecycleOwner, Observer { handleUI(it) })
        orderFragmentViewModel.listenToOrders().observe(viewLifecycleOwner, Observer { handleData(it) })
    }

    private fun handleData(it : List<Order>){
        rv_home.adapter?.let {adapter ->
            if (adapter is OrderAdapter){
                adapter.changelist(it)
            }

        }
    }

    private fun handleUI(it : OrderFragmentState){
        when(it){
            is OrderFragmentState.IsLoading -> {
                if (it.state){
                    pb_home.visibility = View.GONE
                    pb_home.isIndeterminate = false
                }else{
                    pb_home.visibility = View.VISIBLE
                    pb_home.isIndeterminate = true
                }
            }
            is OrderFragmentState.ShowToast -> toast(it.message!!)
        }
    }

    private fun toast(message : String) { Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show() }

    override fun onResume() {
        super.onResume()
        orderFragmentViewModel.getOrders(Constants.getToken(requireActivity()))
    }
}