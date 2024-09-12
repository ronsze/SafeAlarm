package kr.sdbk.data.mapper

import com.google.gson.Gson
import kr.sdbk.data.dto.MissingInfoDTO
import kr.sdbk.domain.model.ward.Location
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.model.ward.WardInfo

object MissingMapper {
    fun MissingInfo.toDTO() = MissingInfoDTO(
        missingTime = missingTime,
        lastLocation = lastLocation,
        signalment = signalment,
        extra = extra,
        wardInfo = Gson().toJson(wardInfo)
    )

    fun MissingInfoDTO.toData() = MissingInfo(
        missingTime = missingTime,
        lastLocation = lastLocation,
        signalment = signalment,
        extra = extra,
        wardInfo = Gson().fromJson(wardInfo, WardInfo::class.java)
    )
}