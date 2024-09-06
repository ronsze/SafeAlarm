package kr.sdbk.data.mapper

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kr.sdbk.data.dto.UserProfileDTO
import kr.sdbk.domain.model.user.User
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.model.user.UserRole

object UserMapper {
    suspend fun FirebaseUser.toUser(refreshToken: Boolean) = User(
        uid = uid,
        token = getIdToken(refreshToken).await().token
    )

    fun UserProfileDTO.toData() = UserProfile(
        uid = uid,
        partnerId = partnerId,
        role = role?.run { UserRole.valueOf(this) }
    )

    fun UserProfile.toDTO() = UserProfileDTO(
        uid = uid,
        partnerId = partnerId,
        role = role?.name
    )
}