package kr.sdbk.domain.model.ward

import kr.sdbk.domain.model.Gender

data class WardInfo(
    var imageUri: String,
    val name: String,
    val gender: Gender,
    val height: String,
    val age: String,
    val guardNumber: String
)