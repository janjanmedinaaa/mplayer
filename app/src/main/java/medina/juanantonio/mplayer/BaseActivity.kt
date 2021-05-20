package medina.juanantonio.mplayer

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_base.*
import medina.juanantonio.mplayer.common.extensions.mainFragment
import medina.juanantonio.mplayer.common.extensions.movieSearchFragment
import medina.juanantonio.mplayer.data.managers.FMoviesManager
import medina.juanantonio.mplayer.data.models.FEpisode
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.features.browse.MainFragment
import medina.juanantonio.mplayer.features.browse.MainViewModel

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), FMoviesManager.ResultsListener {

    open val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        viewModel.fMoviesManager =
            FMoviesManager(this, parentView, listener = this, showSeries = true)
        lifecycle.addObserver(viewModel.fMoviesManager)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Back presses are all handled through onBackPressed.
        //
        // Note: on device, back presses emit one KEYCODE_BACK. On emulator, they
        // emit one KEYCODE_BACK **AND** one KEYCODE_DEL. We short on both to make
        // code paths consistent between the two.
        if (event.keyCode == KeyEvent.KEYCODE_BACK ||
            event.keyCode == KeyEvent.KEYCODE_DEL
        ) return super.dispatchKeyEvent(event)

        return when (supportFragmentManager.fragments[0]) {
            is MainFragment -> {
                supportFragmentManager.mainFragment().dispatchKeyEvent(event) ||
                        super.dispatchKeyEvent(event)
            }
            else -> {
                supportFragmentManager.movieSearchFragment().dispatchKeyEvent(event) ||
                        super.dispatchKeyEvent(event)
            }
        }
    }

    override fun onMovieUrlReceived(fItem: FItem, url: String?) {
        viewModel.movieUrlResults.value = Pair(fItem, url)
    }

    override fun onMovieListReceived(list: List<FItem>, pagination: Boolean) {
    }

    override fun onEpisodeListReceived(list: List<FEpisode>) {
        viewModel.episodeListResults.value = list
    }

    override fun onProgressChanged(
        progressStatus: FMoviesManager.ProgressStatus,
        message: String
    ) {
        when (progressStatus) {
            FMoviesManager.ProgressStatus.START -> {
                cardViewProgress.isVisible = true
            }
            FMoviesManager.ProgressStatus.PAGE_LOOKUP -> {
                textviewProgressInfo.text = message
            }
            FMoviesManager.ProgressStatus.DONE -> {
                cardViewProgress.isVisible = false
            }
        }
    }
}