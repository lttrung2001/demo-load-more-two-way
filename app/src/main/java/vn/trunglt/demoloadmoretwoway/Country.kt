package vn.trunglt.demoloadmoretwoway

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("country") val country: String,
    @SerializedName("region") var region: String
)