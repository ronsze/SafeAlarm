package kr.sdbk.domain.usecase.user_auth

import kotlinx.coroutines.CoroutineScope

interface LoginUseCase {
    operator fun invoke(
        email: String,
        password: String,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    )
}