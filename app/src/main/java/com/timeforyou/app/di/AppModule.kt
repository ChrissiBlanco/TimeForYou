package com.timeforyou.app.di

import android.content.Context
import com.timeforyou.app.BuildConfig
import com.timeforyou.app.data.coach.NoOpCoachAdviceRepository
import com.timeforyou.app.data.coach.OpenAiCoachAdviceRepository
import com.timeforyou.app.data.local.AppDatabase
import com.timeforyou.app.data.local.BehaviorLogDao
import com.timeforyou.app.data.repository.TimeRepositoryImpl
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import com.timeforyou.app.domain.repository.TimeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideBehaviorLogDao(db: AppDatabase): BehaviorLogDao = db.behaviorLogDao()

    @Provides
    @Singleton
    fun provideTimeRepository(dao: BehaviorLogDao): TimeRepository = TimeRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideCoachAdviceRepository(client: OkHttpClient): CoachAdviceRepository =
        if (BuildConfig.OPENAI_API_KEY.isBlank()) {
            NoOpCoachAdviceRepository()
        } else {
            OpenAiCoachAdviceRepository(
                apiKey = BuildConfig.OPENAI_API_KEY,
                model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4o-mini" },
                client = client,
            )
        }
}
