package vn.trunglt.demoloadmoretwoway.responses

import com.google.gson.annotations.SerializedName
import vn.trunglt.demoloadmoretwoway.models.User

data class GetListUserResponse(
    @SerializedName("items") val items: List<User>
)