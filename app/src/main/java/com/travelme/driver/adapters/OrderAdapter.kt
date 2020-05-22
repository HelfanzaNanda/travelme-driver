package com.travelme.driver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.travelme.driver.R
import com.travelme.driver.models.Order

class OrderAdapter(private var orders : MutableList<Order>, private var context: Context)
    : RecyclerView.Adapter<OrderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order, parent, false))
    }

    override fun getItemCount(): Int  = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(orders[position], context)

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(order: Order, context: Context){

        }
    }

}