package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.WardInfo

interface UpdateWardInfoUseCase {
    operator fun invoke(
        wardInfo: WardInfo,
        scope: CoroutineScope,
        onSuccess: (WardInfo) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}