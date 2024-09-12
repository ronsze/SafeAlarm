package kr.sdbk.data.dto

import com.google.gson.annotations.SerializedName
import kr.sdbk.domain.model.ward.Location

data class MissingInfoDTO(
    @SerializedName("missing_time")
    val missingTime: String,

    @SerializedName("last_location")
    val lastLocation: Location,

    @SerializedName("signalment")
    val signalment: String,

    @SerializedName("extra")
    val extra: String,

    @SerializedName("ward_info")
    val wardInfo: String
)