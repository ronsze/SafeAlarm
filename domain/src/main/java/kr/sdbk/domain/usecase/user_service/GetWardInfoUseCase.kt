package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.WardInfo

interface GetWardInfoUseCase {
    operator fun invoke(
        scope: CoroutineScope,
        onSuccess: (WardInfo) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}