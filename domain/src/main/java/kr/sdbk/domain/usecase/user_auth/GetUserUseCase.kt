package kr.sdbk.domain.usecase.user_auth

import kotlinx.coroutines.CoroutineScope
import kr.sdbk.domain.model.user.User

interface GetUserUseCase {
    operator fun invoke(
        refreshToken: Boolean = false,
        scope: CoroutineScope,
        onSuccess: (User?) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}