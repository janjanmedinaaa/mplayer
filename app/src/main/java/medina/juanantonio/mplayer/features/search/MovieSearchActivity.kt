package medina.juanantonio.mplayer.features.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.BaseActivity
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.movieSearchFragment
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.features.server.MServer
import javax.inject.Inject

@AndroidEntryPoint
class MovieSearchActivity : BaseActivity() {

    companion object {
        const val SEARCH_QUERY_EXTRA = "SearchQueryExtra"

        fun getIntent(activity: FragmentActivity, query: String?): Intent {
            return Intent(activity, MovieSearchActivity::class.java).apply {
                query?.let {
                    putExtra(SEARCH_QUERY_EXTRA, it)
                }
            }
        }
    }

    @Inject
    lateinit var mServer: MServer

    override val viewModel: MovieSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().run {
            add(R.id.fragment, MovieSearchFragment(), MovieSearchFragment.FRAGMENT_TAG)
            commit()
        }

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is MServer.MServerListener) {
                mServer.mServerListener = fragment
            }
        }
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.movieSearchFragment().let {
            mServer.mServerListener = it
        }
    }

    override fun onPause() {
        mServer.mServerListener = null
        super.onPause()
    }

    override fun onMovieListReceived(list: List<FItem>, pagination: Boolean) {
        viewModel.handleSearchResults(list)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}