package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope

interface DeleteWardInfoUseCase {
    operator fun invoke(
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    )
}