package medina.juanantonio.mplayer.features.media

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.MPlayerApplication
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.features.server.MServerListener

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "VideoActivity"
    }

    private var mServerListener: MServerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportFragmentManager.beginTransaction().run {
            add(R.id.videoFragment, VideoConsumptionFragment())
            commit()
        }

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is MServerListener) {
                mServerListener = fragment
                (application as MPlayerApplication).mServer.mServerListener = mServerListener
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mServerListener?.let {
            (application as MPlayerApplication).mServer.mServerListener = it
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