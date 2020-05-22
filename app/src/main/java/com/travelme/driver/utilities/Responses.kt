package com.travelme.driver.utilities

import com.google.gson.annotations.SerializedName

data class WrappedResponse<T>(
    @SerializedName("message") var message : String?,
    @SerializedName("status") var status : Boolean?,
    @SerializedName("data") var data : T?
)

data class WrappedListResponse<T>(
    @SerializedName("message") var message : String?,
    @SerializedName("status") var status : Boolean?,
    @SerializedName("data") var data : List<T>?
)