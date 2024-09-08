package kr.sdbk.data.repository.user_service

import kr.sdbk.data.dto.MissingInfoDTO
import kr.sdbk.data.dto.UserProfileDTO
import kr.sdbk.data.dto.WardInfoDTO
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.model.ward.WardInfo

class UserServiceRepositoryImpl(
    private val dataSource: UserServiceDataSource
): UserServiceRepository {
    override suspend fun updateUserProfile(profile: UserProfileDTO) = dataSource.updateUserProfile(profile)
    override suspend fun getUserProfile(): UserProfileDTO = dataSource.getUserProfile()
    override suspend fun updateWardInfo(wardInfo: WardInfoDTO) = dataSource.updateWardInfo(wardInfo)
    override suspend fun getWardInfo(): WardInfoDTO = dataSource.getWardInfo()
    override suspend fun deleteWardInfo() = dataSource.deleteWardInfo()
    override suspend fun postMissing(missingInfo: MissingInfoDTO) = dataSource.postMissing(missingInfo)
}