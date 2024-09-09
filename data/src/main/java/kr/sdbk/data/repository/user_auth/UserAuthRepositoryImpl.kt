package kr.sdbk.data.repository.user_auth

import javax.inject.Inject

class UserAuthRepositoryImpl @Inject constructor(
    private val dataSource: UserAuthDataSource
): UserAuthRepository {
    override suspend fun getUser() = dataSource.getUser()
    override suspend fun signUp(email: String, password: String) = dataSource.signUp(email, password)
    override suspend fun login(email: String, password: String) = dataSource.login(email, password)
}