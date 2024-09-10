package kr.sdbk.domain.model.user

data class UserProfile(
    val uid: String,
    var partnerId: String?,
    var role: UserRole?
)