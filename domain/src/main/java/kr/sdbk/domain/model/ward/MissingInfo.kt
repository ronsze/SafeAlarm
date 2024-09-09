package kr.sdbk.domain.model.ward

import java.io.Serializable

data class MissingInfo(
    val missingTime: String,
    val lastLocation: Location,
    val signalment: String,
    val extra: String,
    val wardInfo: WardInfo
): Serializable