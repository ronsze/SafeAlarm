package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.MissingInfo

interface PostMissingUseCase {
    operator fun invoke(
        missingInfo: MissingInfo,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    )
}