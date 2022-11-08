package moe.chen.budgeteer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.chen.budgeteer.room.AppDatabase
import moe.chen.budgeteer.room.BudgetEntryDao
import moe.chen.budgeteer.room.CategoryDao
import moe.chen.budgeteer.room.UserDao
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences>
        by preferencesDataStore("budgeteer_preferences")

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao =
        appDatabase.categoryDao()

    @Singleton
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao =
        appDatabase.userDao()

    @Singleton
    @Provides
    fun provideBudgetEntryDao(appDatabase: AppDatabase): BudgetEntryDao =
        appDatabase.budgetEntryDao()

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "budgeteer_db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userDataStore
}