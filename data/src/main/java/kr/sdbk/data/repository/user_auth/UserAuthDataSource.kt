package kr.sdbk.data.repository.user_auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserAuthDataSource: UserAuthRepository {
    override suspend fun getUser() = Firebase.auth.currentUser
}