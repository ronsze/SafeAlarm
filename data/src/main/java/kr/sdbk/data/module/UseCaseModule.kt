package kr.sdbk.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.data.usecase.user_auth.GetUserUseCaseImpl
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Provides
    @Singleton
    fun providesGetUserUseCase(repository: UserAuthRepository): GetUserUseCase = GetUserUseCaseImpl(repository)
}