package medina.juanantonio.mplayer.data.managers

import medina.juanantonio.mplayer.data.database.MPlayerDb
import medina.juanantonio.mplayer.data.models.VideoCard

class DatabaseManager(mPlayerDb: MPlayerDb) {
    private val videoCardDao = mPlayerDb.videoCardDao()

    suspend fun addVideoCard(videoCard: VideoCard) {
        videoCardDao.insert(videoCard)
    }

    suspend fun getVideoCards(): List<VideoCard> {
        return videoCardDao.getAll()
    }
}