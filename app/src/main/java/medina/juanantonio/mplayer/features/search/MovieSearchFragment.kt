package medina.juanantonio.mplayer.features.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.createDialogIntent
import medina.juanantonio.mplayer.data.models.FEpisode
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.data.models.FMovie
import medina.juanantonio.mplayer.data.models.ParcelableFItem
import medina.juanantonio.mplayer.data.presenters.CardPresenterSelector
import medina.juanantonio.mplayer.features.browse.MainActivity
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE
import medina.juanantonio.mplayer.features.media.VideoActivity
import medina.juanantonio.mplayer.features.server.MServer
import medina.juanantonio.mplayer.features.webview.WebViewActivity

@AndroidEntryPoint
class MovieSearchFragment :
    SearchSupportFragment(),
    SearchSupportFragment.SearchResultProvider,
    OnItemViewClickedListener,
    MServer.MServerListener {

    companion object {
        const val FRAGMENT_TAG = "MovieSearchFragment"
    }

    private val viewModel: MovieSearchViewModel by activityViewModels()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var startForResultWebView: ActivityResultLauncher<Intent>
    private var requestDelayJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRowAdapter()
        setSearchResultProvider(this)
        setOnItemViewClickedListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        startForResultWebView = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLongOrNull()) {
                ACTION_ID_POSITIVE -> {
                    viewModel.selectedFItem?.let { fItem ->
                        if (fItem is ParcelableFItem) {
                            val intent = WebViewActivity.getIntent(
                                requireActivity(),
                                fItem
                            )
                            activity?.startActivity(intent)
                        }
                    }
                }
            }
        }

        val searchQuery =
            activity?.intent?.getStringExtra(MovieSearchActivity.SEARCH_QUERY_EXTRA)

        searchQuery?.let {
            setSearchQuery(it, true)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.movieUrlResults.observe(viewLifecycleOwner) { (fItem, url) ->
            if (url == null) {
                if (fItem !is FMovie) return@observe
                val intent = requireContext().createDialogIntent(
                    title = getString(R.string.play_to_browser_title),
                    description = getString(R.string.play_to_browser_confirmation_title),
                )
                val activityOptionsCompat = makeSceneTransitionAnimation(requireActivity())
                startForResultWebView.launch(intent, activityOptionsCompat)
            } else {
                playVideo(fItem, url)
            }
        }

        viewModel.episodeListResults.observe(viewLifecycleOwner) {
            displayEpisodes(ArrayList(it))
        }

        viewModel.updateAdapterList.observe(viewLifecycleOwner) { (list, _) ->
            refreshSearchList(viewModel.searchQuery, list = list)
        }
    }

    override fun onSearchRequestReceived(query: String) {
        setSearchQuery(query, true)
    }

    private fun setupRowAdapter() {
        val cardPresenterSelector = CardPresenterSelector(activity)
        mAdapter = ArrayObjectAdapter(cardPresenterSelector)
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        refreshSearchList(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        refreshSearchList(query, immediate = true)
        return true
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder,
        item: Any,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        if (item is FItem) viewModel.clickItem(item)
    }

    fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_MENU) {
            when (keyEvent.action) {
                KeyEvent.ACTION_DOWN -> {
                    viewModel.stopFMoviesManager()
                }
            }
            return true
        }
        return false
    }

    private fun playVideo(fItem: FItem, url: String) {
        if (fItem.videoUrl.isBlank()) return
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val intent = VideoActivity.getIntent(requireActivity(), fItem, url)
                val bundle = makeSceneTransitionAnimation(requireActivity()).toBundle()
                activity?.startActivity(intent, bundle)
            }
        }
    }

    private fun displayEpisodes(list: ArrayList<FEpisode>) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val intent = MainActivity.getIntent(
                    requireActivity(),
                    list,
                    viewModel.selectedFItem?.title ?: ""
                )
                val bundle = makeSceneTransitionAnimation(requireActivity()).toBundle()
                activity?.startActivity(intent, bundle)
            }
        }
    }

    private fun refreshSearchList(
        input: String,
        immediate: Boolean = false,
        list: List<FItem>? = null
    ) {
        // Refresh adapters and only update for the header item to display inputs
        rowsAdapter.clear()
        mAdapter.clear()
        mAdapter.addAll(mAdapter.size(), list ?: emptyList<FItem>())
        viewModel.searchQuery = input

        val headerItem = HeaderItem(getString(R.string.search_title, input))
        rowsAdapter.add(ListRow(headerItem, mAdapter))

        // Create a job that refreshes on every input and starts
        // request after 3s
        if (list.isNullOrEmpty()) {
            requestDelayJob?.cancel()
            requestDelayJob = viewModel.viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    if (!immediate) delay(3000)
                    viewModel.searchQuery(input)
                }
            }
            requestDelayJob?.start()
        }
    }
}