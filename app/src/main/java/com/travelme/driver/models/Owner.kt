package com.travelme.driver.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Owner(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("license_number") var license_number: String? = null,
    @SerializedName("business_name") var business_name: String? = null,
    @SerializedName("business_owner") var business_owner: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("photo") var photo: String? = null,
    @SerializedName("telephone") var telp: String? = null,
    @SerializedName("domicile") var domicile : String? = null,
    @SerializedName("cars") var cars: List<Car> = mutableListOf()
) : Parcelable