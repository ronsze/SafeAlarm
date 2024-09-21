package kr.sdbk.data.repository.user_service

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.sdbk.data.dto.MissingInfoDTO
import kr.sdbk.data.dto.UserProfileDTO
import kr.sdbk.data.dto.WardInfoDTO
import kr.sdbk.domain.model.ward.WardLocation

class UserServiceDataSource: UserServiceRepository {
    companion object {
        private const val PROFILE_DOCUMENT = "profile"
        private const val WARD_INFO_DOCUMENT = "ward_info"
        private const val MISSING_TABLE = "missing"
        private const val LOCATION_TABLE = "location_table"
    }
    private val firestore = Firebase.firestore

    override suspend fun updateUserProfile(profile: UserProfileDTO) {
        firestore.collection(PROFILE_DOCUMENT)
            .document(profile.uid)
            .set(profile)
            .await()
    }

    override suspend fun getUserProfile(): UserProfileDTO {
        return Firebase.auth.currentUser?.uid?.run {
            Log.e("Error", "1")
            firestore.collection(PROFILE_DOCUMENT)
                .document(this)
                .get()
                .await()
                .toObject(UserProfileDTO::class.java) ?: UserProfileDTO(this)
        } ?: throw Exception("Invalid user")
    }

    override suspend fun updateWardInfo(wardInfo: WardInfoDTO): WardInfoDTO {
        val uid = Firebase.auth.currentUser?.uid

        return uid?.run {
            val imageUri: String =
                if (!wardInfo.imageUri.contains("firebasestorage")) withContext(Dispatchers.IO) { uploadImageToStorage(uid, wardInfo.imageUri) }.toString()
                else wardInfo.imageUri
            wardInfo.imageUri = imageUri

            firestore.collection(WARD_INFO_DOCUMENT)
                .document(uid)
                .set(wardInfo)
                .await()

            return wardInfo
        } ?: throw Exception("Invalid user")
    }

    private suspend fun uploadImageToStorage(uid: String, uri: String): Uri {
        val storageRef = Firebase.storage.reference
        val child = storageRef.child("ward_images/$uid.jpg")
        child.putFile(uri.toUri()).await()

        return child.downloadUrl.await()
    }

    override suspend fun getWardInfo(): WardInfoDTO {
        val uid = Firebase.auth.currentUser?.uid
        return uid?.run {
            firestore.collection(WARD_INFO_DOCUMENT)
                .document(uid)
                .get()
                .await()
                .toObject(WardInfoDTO::class.java)
        } ?: throw Exception("Invalid user")
    }

    override suspend fun deleteWardInfo() {
        val uid = Firebase.auth.currentUser?.uid
        uid?.run {
            firestore.collection(WARD_INFO_DOCUMENT)
                .document(uid)
                .delete()
                .await()
        } ?: throw Exception("Invalid user")
    }

    override suspend fun postMissing(missingInfo: MissingInfoDTO) {
        val uid = Firebase.auth.currentUser?.uid
        val database = Firebase.database.reference

        uid?.run {
            database
                .child(MISSING_TABLE)
                .child(uid)
                .setValue(missingInfo)
                .await()
        } ?: throw Exception("Invalid user")
    }

    override suspend fun getMissingList(): List<MissingInfoDTO> {
        val uid = Firebase.auth.currentUser?.uid
        val database = Firebase.database.reference

        return uid?.run {
            database
                .child(MISSING_TABLE)
                .get()
                .await()
                .children
                .map { it.getValue(MissingInfoDTO::class.java) }
            listOf()
        } ?: throw Exception("Invalid user")
    }
}