package vn.trunglt.demoloadmoretwoway.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("avatar_url") val avatarUrl: String
)