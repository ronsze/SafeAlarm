package kr.sdbk.domain.usecase.user_service

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.user.UserProfile

interface GetUserProfileUseCase {
    operator fun invoke(
        scope: CoroutineScope,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}