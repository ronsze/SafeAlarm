package kr.sdbk.data.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.UserMapper.toDTO
import kr.sdbk.data.mapper.WardInfoMapper.toDTO
import kr.sdbk.data.mapper.WardInfoMapper.toData
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.domain.model.ward.WardInfo
import kr.sdbk.domain.usecase.user_service.UpdateWardInfoUseCase

class UpdateWardInfoUseCaseImpl(
    private val repository: UserServiceRepository
): UpdateWardInfoUseCase {
    override fun invoke(
        wardInfo: WardInfo,
        scope: CoroutineScope,
        onSuccess: (WardInfo) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.updateWardInfo(wardInfo.toDTO())
                }
            }
            result.onSuccess { onSuccess(it.toData()) }
            result.onFailure { onFailure(it) }
        }
    }
}