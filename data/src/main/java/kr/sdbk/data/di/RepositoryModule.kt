package kr.sdbk.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sdbk.data.repository.user_auth.UserAuthDataSource
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.data.repository.user_auth.UserAuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun providesUserAuthRepository(dataSource: UserAuthDataSource): UserAuthRepository = UserAuthRepositoryImpl(dataSource)
}