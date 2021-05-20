package medina.juanantonio.mplayer.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import medina.juanantonio.mplayer.features.server.MServer
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideMServer(): MServer {
        return MServer(8080)
    }
}