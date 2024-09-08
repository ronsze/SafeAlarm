package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.usecase.user_service.DeleteWardInfoUseCase

class DeleteWardInfoUseCaseImpl(
    private val repository: UserServiceRepository
): DeleteWardInfoUseCase {
    override fun invoke(
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.deleteWardInfo()
                }
            }
            result.onSuccess { onSuccess() }
            result.onFailure { onFailure(it) }
        }
    }
}