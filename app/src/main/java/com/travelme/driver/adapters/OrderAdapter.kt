package com.travelme.driver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.travelme.driver.R
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.item_order.view.*

class OrderAdapter(private var orders : MutableList<Order>, private var context: Context)
    : RecyclerView.Adapter<OrderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order, parent, false))
    }

    override fun getItemCount(): Int  = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(orders[position], context)

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(order: Order, context: Context){
            with(itemView){
                txt_name_user.text = order.user.name
                txt_pickup_point_user.text = order.pickup_point
                txt_destination_point_user.text = order.destination_point
                txt_seat.text = Constants.setToIDR(order.total_price!!)
            }
        }
    }

    fun changelist(c : List<Order>){
        orders.clear()
        orders.addAll(c)
        notifyDataSetChanged()
    }

}