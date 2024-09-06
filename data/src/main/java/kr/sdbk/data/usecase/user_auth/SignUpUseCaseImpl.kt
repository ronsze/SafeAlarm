package kr.sdbk.data.usecase.user_auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.domain.usecase.user_auth.SignUpUseCase

class SignUpUseCaseImpl(
    private val repository: UserAuthRepository
): SignUpUseCase {
    override fun invoke(
        email: String,
        password: String,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    repository.signUp(email, password)
                }
            }
            result.onSuccess { onSuccess() }
            result.onFailure { onFailure(it) }
        }
    }
}