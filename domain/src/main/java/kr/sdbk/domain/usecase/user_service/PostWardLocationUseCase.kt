package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.WardLocation

interface PostWardLocationUseCase {
    operator fun invoke(
        wardLocation: WardLocation,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    )
}