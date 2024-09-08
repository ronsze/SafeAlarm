package kr.sdbk.data.dto

import com.google.gson.annotations.SerializedName
import kr.sdbk.domain.model.Gender

data class WardInfoDTO(
    @SerializedName("image_uri")
    var imageUri: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("height")
    val height: String,

    @SerializedName("age")
    val age: String,

    @SerializedName("guard_number")
    val guardNumber: String
)