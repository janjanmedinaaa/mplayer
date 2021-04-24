package medina.juanantonio.mplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import medina.juanantonio.mplayer.data.models.VideoCard

@Dao
interface VideoCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(videoCard: VideoCard)

    @Query("SELECT * FROM VideoCard")
    suspend fun getAll(): List<VideoCard>
}
