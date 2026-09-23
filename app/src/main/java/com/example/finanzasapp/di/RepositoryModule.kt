package com.example.finanzasapp.di

import com.example.finanzasapp.data.FinanzasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFinanzasRepository(
        supabaseClient: SupabaseClient
    ): FinanzasRepository = FinanzasRepository(supabaseClient)
}