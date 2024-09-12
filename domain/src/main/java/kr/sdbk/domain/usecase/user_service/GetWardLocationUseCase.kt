package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.WardLocation

interface GetWardLocationUseCase {
    operator fun invoke(
        scope: CoroutineScope,
        onSuccess: (WardLocation) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}