package com.example.finanzasapp.di

import com.example.finanzasapp.data.repository.CategoryRepository
import com.example.finanzasapp.data.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(): ExpenseRepository = ExpenseRepository()

    @Provides
    @Singleton
    fun provideCategoryRepository(): CategoryRepository = CategoryRepository()
}