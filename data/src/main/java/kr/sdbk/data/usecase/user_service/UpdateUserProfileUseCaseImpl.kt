package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.UserMapper.toDTO
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.usecase.user_service.UpdateUserProfileUseCase

class UpdateUserProfileUseCaseImpl(
    private val repository: UserServiceRepository
): UpdateUserProfileUseCase {
    override fun invoke(
        profile: UserProfile,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.updateUserProfile(profile.toDTO())
                }
            }
            result.onSuccess { onSuccess() }
            result.onFailure { onFailure(it) }
        }
    }
}