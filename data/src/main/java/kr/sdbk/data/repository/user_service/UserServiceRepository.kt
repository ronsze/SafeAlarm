package kr.sdbk.data.repository.user_service

import kr.sdbk.data.dto.UserProfileDTO

interface UserServiceRepository {
    suspend fun updateUserProfile(profile: UserProfileDTO)
    suspend fun getUserProfile(): UserProfileDTO
}