package kr.sdbk.data.repository.user_auth

import com.google.firebase.auth.FirebaseUser

interface UserAuthRepository {
    suspend fun getUser(): FirebaseUser?
}