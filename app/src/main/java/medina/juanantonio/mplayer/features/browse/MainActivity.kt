package medina.juanantonio.mplayer.features.browse

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.BaseActivity
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.mainFragment
import medina.juanantonio.mplayer.data.models.FEpisode
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.features.server.MServer
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var mServer: MServer

    companion object {
        const val EPISODE_LIST_EXTRA = "EpisodeListExtra"
        const val SERIES_TITLE_EXTRA = "SeriesTitleExtra"

        fun getIntent(
            activity: FragmentActivity,
            list: ArrayList<FEpisode>,
            videoTitle: String
        ): Intent {
            return Intent(activity, MainActivity::class.java).apply {
                putExtra(SERIES_TITLE_EXTRA, videoTitle)
                putParcelableArrayListExtra(EPISODE_LIST_EXTRA, list)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().run {
            add(R.id.fragment, MainFragment(), MainFragment.FRAGMENT_TAG)
            commit()
        }

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is MServer.MServerListener) {
                mServer.mServerListener = fragment
            }
        }
    }

    override fun onMovieListReceived(list: List<FItem>, pagination: Boolean) {
        viewModel.handleNewMovieList(list, pagination)
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.mainFragment().let {
            mServer.mServerListener = it
        }
    }

    override fun onPause() {
        mServer.mServerListener = null
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
