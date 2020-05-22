package com.travelme.driver.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Car(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("number_plate") var number_plate: String? = null,
    @SerializedName("facility") var facility: String? = null,
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("status") var status: Boolean = false
) : Parcelable