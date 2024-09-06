package kr.sdbk.domain.usecase.user_auth

import kr.sdbk.domain.model.User

interface GetUserUseCase {
    suspend operator fun invoke(
        refreshToken: Boolean = false,
        onSuccess: (User?) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}