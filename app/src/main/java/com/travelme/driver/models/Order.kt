package com.travelme.driver.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("hour") var hour: String? = null,
    @SerializedName("price") var price: Int? = null,
    @SerializedName("total_price") var total_price: Int? = null,
    @SerializedName("total_seat") var total_seat : Int? = null,
    @SerializedName("pickup_point") var pickup_point : String? = null,
    @SerializedName("lat_pickup_point") var lat_pickup_point : String? = null,
    @SerializedName("lng_pickup_point") var lng_pickup_point : String? = null,
    @SerializedName("destination_point") var destination_point : String? = null,
    @SerializedName("lat_destination_point") var lat_destination_point : String? = null,
    @SerializedName("lng_destination_point") var lng_destination_point : String? = null,
    @SerializedName("status") var status : String? = null,
    @SerializedName("arrived") var arrived : Boolean? = false,
    @SerializedName("done") var done : Boolean? = false,
    @SerializedName("user") var user : User,
    @SerializedName("owner") var owner : Owner,
    @SerializedName("departure") var departure : Departure,
    @SerializedName("driver") var driver : Driver,
    @SerializedName("car") var car : Car
) : Parcelable

@Parcelize
data class OrderForSchedulle(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("hour") var hour: String? = null,
    @SerializedName("total_user") var total_user : Int? = null,
    @SerializedName("is_order") var is_order : Boolean = false,
    @SerializedName("departure") var departure : Departure? = null
) : Parcelable