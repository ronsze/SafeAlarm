package kr.sdbk.data.usecase.user_auth

import kr.sdbk.data.mapper.UserMapper.toUser
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.domain.model.User
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase

class GetUserUseCaseImpl(
    private val repository: UserAuthRepository
): GetUserUseCase {
    override suspend operator fun invoke(
        refreshToken: Boolean,
        onSuccess: (User?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val result = kotlin.runCatching {
            val firebaseUser = repository.getUser()
            firebaseUser?.toUser(refreshToken)
        }
        result.onSuccess { onSuccess(it) }
        result.onFailure { onFailure(it) }
    }
}