package com.travelme.driver.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Departure(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("from") var from: String? = null,
    @SerializedName("destination") var destination: String? = null,
    @SerializedName("photo_destination") var photo_destination: String? = null,
    @SerializedName("price") var price: Int? = null,
    @SerializedName("owner") var owner: Owner
) : Parcelable
