package com.kuemiin.reversevoice.di

import android.content.Context
import androidx.room.Room
import com.kuemiin.reversevoice.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
//    @Singleton
//    @Provides
//    fun provideRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BuildConfig.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app, AppDatabase::class.java, "reverse")
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration().build()
//

    @Singleton
    @Provides
    fun provideDao(db: AppDatabase) = db.myCreationDao()

//    @Provides
//    @BaseUrlInfo
//    internal fun provideBaseUrl(): String = BuildConfig.BASE_URL_API
//    @Provides
//    @Singleton
//    fun provideApiService(retrofit: Retrofit) = retrofit.create(NetworkService::class.java)
}