package kr.sdbk.domain.model.ward

data class MissingInfo(
    val missingTime: String,
    val lastLocation: String,
    val signalment: String,
    val extra: String,
    val wardInfo: WardInfo
)