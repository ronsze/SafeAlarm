package kr.sdbk.data.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDTO(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("partner_id")
    val partnerId: String? = null,

    @SerializedName("role")
    val role: String? = null
)