package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.MissingMapper.toDTO
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.usecase.user_service.PostMissingUseCase

class PostMissingUseCaseImpl(
    private val repository: UserServiceRepository
): PostMissingUseCase {
    override fun invoke(
        missingInfo: MissingInfo,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.postMissing(missingInfo.toDTO())
                }
            }
            result.onSuccess { onSuccess() }
            result.onFailure { onFailure(it) }
        }
    }
}