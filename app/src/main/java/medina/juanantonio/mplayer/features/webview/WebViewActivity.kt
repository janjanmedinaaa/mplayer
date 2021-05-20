package medina.juanantonio.mplayer.features.webview

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_base.*
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.createDialogIntent
import medina.juanantonio.mplayer.data.managers.FMoviesManager
import medina.juanantonio.mplayer.data.models.FEpisode
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.features.browse.MainViewModel
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity(), FMoviesManager.ResultsListener {

    companion object {
        const val VIDEO_URL_EXTRA = "VideoUrlExtra"

        fun getIntent(
            activity: FragmentActivity,
            videoUrl: String
        ): Intent {
            return Intent(activity, WebViewActivity::class.java).apply {
                putExtra(VIDEO_URL_EXTRA, videoUrl)
            }
        }
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var startForResultClosePlayer: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.fMoviesManager = FMoviesManager(this, parentView, listener = this)
        lifecycle.addObserver(viewModel.fMoviesManager)

        startForResultClosePlayer = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLongOrNull()) {
                ACTION_ID_POSITIVE -> finish()
            }
        }

        layoutLoadingVideo.isVisible = true
        webView.isVisible = false

        val videoUrl = intent.getStringExtra(VIDEO_URL_EXTRA)
        if (videoUrl == null) {
            finish()
            return
        }

        viewModel.fMoviesManager.playFromBrowser(videoUrl)
    }

    override fun onMovieUrlReceived(fItem: FItem, url: String?) {
    }

    override fun onMovieListReceived(list: List<FItem>, pagination: Boolean) {
    }

    override fun onEpisodeListReceived(list: List<FEpisode>) {
    }

    override fun onWebViewPlayerReady() {
        layoutLoadingVideo.isVisible = false
        webView.isVisible = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = createDialogIntent(
            title = getString(R.string.close_player_confirmation_title),
        )
        val activityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(this@WebViewActivity)
        startForResultClosePlayer.launch(intent, activityOptionsCompat)
    }
}