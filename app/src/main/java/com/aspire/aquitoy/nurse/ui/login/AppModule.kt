package com.aspire.aquitoy.nurse.ui.login

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) // Cambiar a SingletonComponent o el componente adecuado
object AppModule {
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}
