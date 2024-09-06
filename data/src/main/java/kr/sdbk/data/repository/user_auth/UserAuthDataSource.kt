package kr.sdbk.data.repository.user_auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserAuthDataSource: UserAuthRepository {
    private val auth = Firebase.auth

    override suspend fun getUser() = auth.currentUser

    override suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }
}