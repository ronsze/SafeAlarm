package kr.sdbk.data.repository.user_service

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kr.sdbk.data.dto.UserProfileDTO

class UserServiceDataSource: UserServiceRepository {
    companion object {
        private const val PROFILE_DOCUMENT = "profile"
    }
    private val firestore = Firebase.firestore

    override suspend fun updateUserProfile(profile: UserProfileDTO) {
        firestore.collection(PROFILE_DOCUMENT)
            .document(profile.uid)
            .set(profile)
            .await()
    }

    override suspend fun getUserProfile(): UserProfileDTO {
        val uid = Firebase.auth.currentUser?.uid
        return uid?.run {
            firestore.collection(PROFILE_DOCUMENT)
                .document(uid)
                .get()
                .await()
                .toObject(UserProfileDTO::class.java)
        } ?: throw Exception("Invalid user")
    }
}