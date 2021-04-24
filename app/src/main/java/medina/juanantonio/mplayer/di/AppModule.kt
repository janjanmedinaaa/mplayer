package medina.juanantonio.mplayer.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import medina.juanantonio.mplayer.data.database.MPlayerDb
import medina.juanantonio.mplayer.data.managers.DatabaseManager
import medina.juanantonio.mplayer.data.managers.MySQLManager
import medina.juanantonio.mplayer.features.server.MServer
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideMPlayerDb(@ApplicationContext context: Context): MPlayerDb {
        return Room.databaseBuilder(context, MPlayerDb::class.java, "mplayer.db")
            .fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDatabaseManager(mPlayerDb: MPlayerDb): DatabaseManager {
        return DatabaseManager(mPlayerDb)
    }

    @Provides
    @Singleton
    fun provideMServer(): MServer {
        return MServer(8080)
    }

    @Provides
    @Singleton
    fun provideMySQLManager(): MySQLManager {
        return MySQLManager("", "", "", 3306, "")
    }
}