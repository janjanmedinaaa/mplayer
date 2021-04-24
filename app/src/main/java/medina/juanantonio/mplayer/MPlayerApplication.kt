package medina.juanantonio.mplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import medina.juanantonio.mplayer.data.managers.DatabaseManager
import medina.juanantonio.mplayer.features.server.MServer
import javax.inject.Inject

@HiltAndroidApp
class MPlayerApplication : Application() {

    @Inject
    lateinit var mServer: MServer

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun onCreate() {
        super.onCreate()
        mServer.start()
    }
}

