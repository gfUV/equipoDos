package com.wt2dadmuvy.spinbot.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wt2dadmuvy.spinbot.repository.AuthRepository
import com.wt2dadmuvy.spinbot.repository.AuthRepositoryImpl
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import com.wt2dadmuvy.spinbot.repository.ChallengeRepositoryImpl
import com.wt2dadmuvy.spinbot.database.AppDatabase
import com.wt2dadmuvy.spinbot.database.ChallengeDao
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.getDatabase(context)
        }

        @Provides
        @Singleton
        fun provideChallengeDao(database: AppDatabase): ChallengeDao {
            return database.challengeDao()
        }
    }
}
