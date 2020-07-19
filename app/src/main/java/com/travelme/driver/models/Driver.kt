package com.travelme.driver.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Driver(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("nik") var nik: String? = null,
    @SerializedName("sim") var sim: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("api_token") var token: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("avatar") var avatar: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("telephone") var telp: String? = null,
    @SerializedName("active") var active : Boolean? = false,
    @SerializedName("location") var location : String? = null,
    @SerializedName("owner") var owner : Owner,
    @SerializedName("car") var car : Car
) : Parcelable