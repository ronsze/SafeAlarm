package kr.sdbk.data.usecase.user_auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.mapper.UserMapper.toUser
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.domain.model.user.User
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase

class GetUserUseCaseImpl(
    private val repository: UserAuthRepository
): GetUserUseCase {
    override operator fun invoke(
        refreshToken: Boolean,
        scope: CoroutineScope,
        onSuccess: (User?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val firebaseUser = repository.getUser()
                    firebaseUser?.toUser(refreshToken)
                }
            }
            result.onSuccess { onSuccess(it) }
            result.onFailure { onFailure(it) }
        }
    }
}