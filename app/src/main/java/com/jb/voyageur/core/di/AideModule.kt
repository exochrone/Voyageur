package com.jb.voyageur.core.di

import android.content.Context
import com.jb.voyageur.core.ui.helper.AideCaracteristiqueProvider
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import com.jb.voyageur.core.ui.helper.AideRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AideModule {
    @Provides
    @Singleton
    fun provideAideRepository(@ApplicationContext context: Context): AideRepository {
        return AideRepository(context)
    }

    @Provides
    @Singleton
    fun provideAideCompetenceProvider(aideRepository: AideRepository): AideCompetenceProvider {
        return AideCompetenceProvider(aideRepository)
    }

    @Provides
    @Singleton
    fun provideAideCaracteristiqueProvider(aideRepository: AideRepository): AideCaracteristiqueProvider {
        return AideCaracteristiqueProvider(aideRepository)
    }
}
