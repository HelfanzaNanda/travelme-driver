package com.travelme.driver.ui.maps

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.travelme.driver.R
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.item_bottom_sheet.view.*

class BottomSheetMapsAdapter (private var orders : MutableList<Order>, private var context: Context,
                              private var mapsViewModel: MapsViewModel)
    : RecyclerView.Adapter<BottomSheetMapsAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_bottom_sheet, parent, false)
        )
    }

    override fun getItemCount(): Int = orders.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(orders[position], context,mapsViewModel)

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        private var change : Boolean = false

        @SuppressLint("SetTextI18n")
        fun bind(order: Order, context: Context, mapsViewModel: MapsViewModel){
            with(itemView){
                txt_name_user.text = "Nama Pemesan : ${order.user.name}"
                txt_seat.text = "Kursi yang di pesan  : ${order.total_seat}"

                setOnClickListener {
                    if (!change){
                        img_arrow_down.gone()
                        img_arrow_up.visible()
                        label_pickup_point.visible()
                        txt_pickup_point_user.visible()
                        txt_pickup_point_user.text = order.pickup_point
                        label_destination_point.visible()
                        txt_destination_point_user.visible()
                        txt_destination_point_user.text = order.destination_point
                        change = true
                    }else{
                        img_arrow_down.visible()
                        img_arrow_up.gone()
                        label_pickup_point.gone()
                        txt_pickup_point_user.gone()
                        label_destination_point.gone()
                        txt_destination_point_user.gone()
                        change = false
                    }
                }
                if (order.arrived == false){
                    btn_arrived.setOnClickListener {
                        mapsViewModel.arrived(Constants.getToken(context), order.id.toString())
                    }
                }

                if (order.arrived == true && order.done == false){
                    btn_arrived.gone()
                    btn_done.visible()
                    btn_done.setOnClickListener {
                        //Toast.makeText(context, "aaa", Toast.LENGTH_LONG).show()
                        mapsViewModel.done(Constants.getToken(context), order.id.toString())
                    }
                }

                if (order.arrived == true && order.done == true){
                    btn_arrived.gone()
                    btn_done.gone()
                }

            }
        }
    }

    fun changelist(c : List<Order>){
        orders.clear()
        orders.addAll(c)
        notifyDataSetChanged()
    }

}