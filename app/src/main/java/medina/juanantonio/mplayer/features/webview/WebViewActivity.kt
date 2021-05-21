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
import medina.juanantonio.mplayer.data.models.ParcelableFItem
import medina.juanantonio.mplayer.features.browse.MainViewModel
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity(), FMoviesManager.ResultsListener {

    companion object {
        const val PARCELABLE_FITEM_EXTRA = "ParcelableFItemExtra"

        fun getIntent(
            activity: FragmentActivity,
            parcelableFItem: ParcelableFItem
        ): Intent {
            return Intent(activity, WebViewActivity::class.java).apply {
                putExtra(PARCELABLE_FITEM_EXTRA, parcelableFItem)
            }
        }
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel.fMoviesManager = FMoviesManager(this, parentView, listener = this)
        lifecycle.addObserver(viewModel.fMoviesManager)

        layoutLoadingVideo.isVisible = true
        webView.isVisible = false

        val parcelableFItem =
            intent?.getParcelableExtra<ParcelableFItem>(PARCELABLE_FITEM_EXTRA)
        if (parcelableFItem == null) {
            finish()
            return
        }

        viewModel.fMoviesManager.playFromBrowser(parcelableFItem)
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
}