package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.ward.MissingInfo

interface GetMissingListUseCase {
    operator fun invoke(
        scope: CoroutineScope,
        onSuccess: (List<MissingInfo>) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}