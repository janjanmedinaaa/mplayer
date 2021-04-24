package medina.juanantonio.mplayer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import medina.juanantonio.mplayer.BuildConfig
import medina.juanantonio.mplayer.data.database.dao.VideoCardDao
import medina.juanantonio.mplayer.data.models.VideoCard

@Database(
    entities = [VideoCard::class],
    version = BuildConfig.VERSION_CODE
)
abstract class MPlayerDb : RoomDatabase() {
    abstract fun videoCardDao(): VideoCardDao
}
