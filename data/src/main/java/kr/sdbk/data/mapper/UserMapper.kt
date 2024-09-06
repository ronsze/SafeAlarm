package kr.sdbk.data.mapper

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kr.sdbk.domain.model.User

object UserMapper {
    suspend fun FirebaseUser.toUser(refreshToken: Boolean) = User(
        uid = uid,
        token = getIdToken(refreshToken).await().token
    )
}