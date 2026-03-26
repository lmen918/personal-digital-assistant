package com.lmen918.pda.di

import android.content.Context
import androidx.room.Room
import com.lmen918.pda.data.local.PdaDatabase
import com.lmen918.pda.data.local.dao.EventDao
import com.lmen918.pda.data.local.dao.TagDao
import com.lmen918.pda.data.repository.EventRepositoryImpl
import com.lmen918.pda.data.repository.TagRepositoryImpl
import com.lmen918.pda.domain.repository.EventRepository
import com.lmen918.pda.domain.repository.TagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePdaDatabase(@ApplicationContext context: Context): PdaDatabase =
        Room.databaseBuilder(context, PdaDatabase::class.java, "pda_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTagDao(db: PdaDatabase): TagDao = db.tagDao()

    @Provides
    fun provideEventDao(db: PdaDatabase): EventDao = db.eventDao()

    @Provides
    @Singleton
    fun provideTagRepository(impl: TagRepositoryImpl): TagRepository = impl

    @Provides
    @Singleton
    fun provideEventRepository(impl: EventRepositoryImpl): EventRepository = impl
}
