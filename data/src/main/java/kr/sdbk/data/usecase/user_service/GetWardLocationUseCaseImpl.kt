package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.ward.WardLocation
import kr.sdbk.domain.usecase.user_service.GetWardLocationUseCase

class GetWardLocationUseCaseImpl(
    private val repository: UserServiceRepository
): GetWardLocationUseCase {
    override fun invoke(
        scope: CoroutineScope,
        onSuccess: (WardLocation) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.getWardLocation()
                }
            }
            result.onSuccess { onSuccess(it) }
            result.onFailure { onFailure(it) }
        }
    }
}