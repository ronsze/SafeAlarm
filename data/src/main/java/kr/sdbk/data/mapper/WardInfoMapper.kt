package kr.sdbk.data.mapper

import kr.sdbk.data.dto.WardInfoDTO
import kr.sdbk.domain.model.Gender
import kr.sdbk.domain.model.ward.WardInfo
import kotlin.math.sign

object WardInfoMapper {
    fun WardInfo.toDTO() = WardInfoDTO(
        imageUri = imageUri,
        name = name,
        gender = gender.name,
        height = height,
        age = age,
        guardNumber = guardNumber,
        signalment = signalment
    )

    fun WardInfoDTO.toData() = WardInfo(
        imageUri = imageUri,
        name = name,
        gender = Gender.valueOf(gender),
        height = height,
        age = age,
        guardNumber = guardNumber,
        signalment = signalment
    )
}