package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.UserMapper.toData
import kr.sdbk.data.mapper.UserMapper.toUser
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.usecase.user_service.GetUserProfileUseCase

class GetUserProfileUseCaseImpl(
    private val repository: UserServiceRepository
): GetUserProfileUseCase {
    override fun invoke(
        scope: CoroutineScope,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.getUserProfile()
                }
            }
            result.onSuccess { onSuccess(it.toData()) }
            result.onFailure { onFailure(it) }
        }
    }
}