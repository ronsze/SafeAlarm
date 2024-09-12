package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.ward.WardLocation
import kr.sdbk.domain.usecase.user_service.PostWardLocationUseCase

class PostWardLocationUseCaseImpl(
    private val repository: UserServiceRepository
): PostWardLocationUseCase {
    override fun invoke(
        wardLocation: WardLocation,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.postWardLocation(wardLocation)
                }
            }
            result.onSuccess { onSuccess() }
            result.onFailure { onFailure(it) }
        }
    }
}