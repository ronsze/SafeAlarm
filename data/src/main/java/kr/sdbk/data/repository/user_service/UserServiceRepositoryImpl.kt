package kr.sdbk.data.repository.user_service

import kr.sdbk.data.dto.UserProfileDTO
import kr.sdbk.domain.model.user.UserProfile

class UserServiceRepositoryImpl(
    private val dataSource: UserServiceDataSource
): UserServiceRepository {
    override suspend fun updateUserProfile(profile: UserProfileDTO) = dataSource.updateUserProfile(profile)
    override suspend fun getUserProfile(): UserProfileDTO = dataSource.getUserProfile()
}