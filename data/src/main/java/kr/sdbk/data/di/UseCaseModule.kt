package kr.sdbk.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sdbk.data.repository.user_auth.UserAuthRepository
import kr.sdbk.data.repository.user_service.UserServiceRepository
import kr.sdbk.data.usecase.user_auth.GetUserUseCaseImpl
import kr.sdbk.data.usecase.user_service.DeleteWardInfoUseCaseImpl
import kr.sdbk.data.usecase.user_service.GetUserProfileUseCaseImpl
import kr.sdbk.data.usecase.user_service.GetWardInfoUseCaseImpl
import kr.sdbk.data.usecase.user_service.UpdateUserProfileUseCaseImpl
import kr.sdbk.data.usecase.user_service.UpdateWardInfoUseCaseImpl
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import kr.sdbk.domain.usecase.user_service.DeleteWardInfoUseCase
import kr.sdbk.domain.usecase.user_service.GetUserProfileUseCase
import kr.sdbk.domain.usecase.user_service.GetWardInfoUseCase
import kr.sdbk.domain.usecase.user_service.UpdateUserProfileUseCase
import kr.sdbk.domain.usecase.user_service.UpdateWardInfoUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Provides
    @Singleton
    fun providesGetUserUseCase(repository: UserAuthRepository): GetUserUseCase = GetUserUseCaseImpl(repository)
    @Provides
    @Singleton
    fun providesUpdateUserProfileUseCase(repository: UserServiceRepository): UpdateUserProfileUseCase = UpdateUserProfileUseCaseImpl(repository)
    @Provides
    @Singleton
    fun providesGetUserProfileUseCase(repository: UserServiceRepository): GetUserProfileUseCase = GetUserProfileUseCaseImpl(repository)
    @Provides
    @Singleton
    fun providesUpdateWardInfoUseCase(repository: UserServiceRepository): UpdateWardInfoUseCase = UpdateWardInfoUseCaseImpl(repository)
    @Provides
    @Singleton
    fun providesGetWardInfoUseCase(repository: UserServiceRepository): GetWardInfoUseCase = GetWardInfoUseCaseImpl(repository)
    @Provides
    @Singleton
    fun providesDeleteWardInfoUseCase(repository: UserServiceRepository): DeleteWardInfoUseCase = DeleteWardInfoUseCaseImpl(repository)
}