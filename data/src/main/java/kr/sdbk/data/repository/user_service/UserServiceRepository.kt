package kr.sdbk.data.repository.user_service

import kr.sdbk.data.dto.MissingInfoDTO
import kr.sdbk.data.dto.UserProfileDTO
import kr.sdbk.data.dto.WardInfoDTO
import kr.sdbk.domain.model.ward.WardLocation

interface UserServiceRepository {
    suspend fun updateUserProfile(profile: UserProfileDTO)
    suspend fun getUserProfile(): UserProfileDTO

    suspend fun updateWardInfo(wardInfo: WardInfoDTO): WardInfoDTO
    suspend fun getWardInfo(): WardInfoDTO
    suspend fun deleteWardInfo()

    suspend fun postMissing(missingInfo: MissingInfoDTO)
    suspend fun getMissingList(): List<MissingInfoDTO>

    suspend fun postWardLocation(wardLocation: WardLocation)
    suspend fun getWardLocation(): WardLocation
}