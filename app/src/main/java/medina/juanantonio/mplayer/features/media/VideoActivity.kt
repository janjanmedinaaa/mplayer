package medina.juanantonio.mplayer.features.media

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.data.models.FItem

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "VideoActivity"

        fun getIntent(activity: FragmentActivity, fItem: FItem, videoUrl: String): Intent {
            val metaData = MediaMetaData().apply {
                mediaSourcePath = videoUrl
                mediaTitle = fItem.title
                mediaAlbumArtUrl = fItem.imageUrl
            }

            return Intent(activity, VideoActivity::class.java).apply {
                putExtra(TAG, metaData)
                data = Uri.parse(metaData.mediaSourcePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportFragmentManager.beginTransaction().run {
            add(R.id.videoFragment, VideoConsumptionFragment())
            commit()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // This part is necessary to ensure that getIntent returns the latest intent when
        // VideoExampleActivity is started. By default, getIntent() returns the initial intent
        // that was set from another activity that started VideoExampleActivity. However, we need
        // to update this intent when for example, user clicks on another video when the currently
        // playing video is in PIP mode, and a new video needs to be started.
        setIntent(intent)
    }
}