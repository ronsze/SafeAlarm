package kr.sdbk.domain.model.user

data class UserProfile(
    val uid: String,
    val partnerId: String?,
    val role: UserRole?
)