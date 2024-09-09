package kr.sdbk.data.mapper

import com.google.gson.Gson
import kr.sdbk.data.dto.LocationDTO
import kr.sdbk.data.dto.MissingInfoDTO
import kr.sdbk.domain.model.ward.Location
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.model.ward.WardInfo

object MissingMapper {
    fun MissingInfo.toDTO() = MissingInfoDTO(
        missingTime = missingTime,
        lastLocation = lastLocation.toDTO(),
        signalment = signalment,
        extra = extra,
        wardInfo = Gson().toJson(wardInfo)
    )

    fun MissingInfoDTO.toData() = MissingInfo(
        missingTime = missingTime,
        lastLocation = lastLocation.toData(),
        signalment = signalment,
        extra = extra,
        wardInfo = Gson().fromJson(wardInfo, WardInfo::class.java)
    )

    fun Location.toDTO() = LocationDTO(
        latitude = latitude,
        longitude = longitude,
        text = text
    )

    fun LocationDTO.toData() = Location(
        latitude = latitude,
        longitude = longitude,
        text = text
    )
}