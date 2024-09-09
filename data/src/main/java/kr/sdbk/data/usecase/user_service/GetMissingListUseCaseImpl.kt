package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.MissingMapper.toData
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.usecase.user_service.GetMissingListUseCase

class GetMissingListUseCaseImpl(
    private val repository: UserServiceRepository
): GetMissingListUseCase {
    override fun invoke(
        scope: CoroutineScope,
        onSuccess: (List<MissingInfo>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.getMissingList()
                }
            }
            result.onSuccess { onSuccess(it.map { it.toData() }) }
            result.onFailure { onFailure(it) }
        }
    }
}