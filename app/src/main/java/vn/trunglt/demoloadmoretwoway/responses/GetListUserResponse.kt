package vn.trunglt.demoloadmoretwoway.responses

import com.google.gson.annotations.SerializedName
import vn.trunglt.demoloadmoretwoway.models.User

data class GetListUserResponse(
    @SerializedName("documents") val documents: List<User>
)