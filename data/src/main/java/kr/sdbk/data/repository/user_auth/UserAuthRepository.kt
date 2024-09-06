package kr.sdbk.data.repository.user_auth

import com.google.firebase.auth.FirebaseUser

interface UserAuthRepository {
    suspend fun getUser(): FirebaseUser?
    suspend fun signUp(email: String, password: String)
    suspend fun login(email: String, password: String)
}