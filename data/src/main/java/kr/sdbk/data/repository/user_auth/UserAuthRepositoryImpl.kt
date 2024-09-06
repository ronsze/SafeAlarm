package kr.sdbk.data.repository.user_auth

class UserAuthRepositoryImpl(private val dataSource: UserAuthDataSource): UserAuthRepository {
    override suspend fun getUser() = dataSource.getUser()
}