package medina.juanantonio.mplayer.features.browse

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.createDialogIntent
import medina.juanantonio.mplayer.data.models.*
import medina.juanantonio.mplayer.data.presenters.CardPresenterSelector
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE
import medina.juanantonio.mplayer.features.media.VideoActivity
import medina.juanantonio.mplayer.features.search.MovieSearchActivity
import medina.juanantonio.mplayer.features.server.MServer
import medina.juanantonio.mplayer.features.webview.WebViewActivity

@AndroidEntryPoint
class MainFragment :
    VerticalGridSupportFragment(),
    OnItemViewClickedListener,
    OnItemViewSelectedListener,
    MServer.MServerListener {

    companion object {
        const val FRAGMENT_TAG = "MainFragment"
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var startForResultCloseApp: ActivityResultLauncher<Intent>
    private lateinit var startForResultWebView: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        startForResultCloseApp = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLongOrNull()) {
                ACTION_ID_POSITIVE -> activity?.finish()
            }
        }

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

        setupRowAdapter()
        searchAffordanceColor =
            ContextCompat.getColor(requireContext(), R.color.search_button_color)

        val searchResultsEpisodeList =
            activity?.intent?.getParcelableArrayListExtra<FEpisode>(
                MainActivity.EPISODE_LIST_EXTRA
            )
        val searchResultsSeriesTitle =
            activity?.intent?.getStringExtra(MainActivity.SERIES_TITLE_EXTRA)

        if (!searchResultsEpisodeList.isNullOrEmpty()) {
            viewModel.handleFEpisodeList(searchResultsEpisodeList, searchResultsSeriesTitle)
        } else {
            setupBackButtonCallback()
            setOnSearchClickedListener {
                val intent = MovieSearchActivity.getIntent(requireActivity(), null)
                val bundle = makeSceneTransitionAnimation(requireActivity()).toBundle()
                activity?.startActivity(intent, bundle)
            }

            // Load the first page, either from search or browse movies
            viewModel.loadMovieList(page = 1)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewTitle.observe(viewLifecycleOwner) {
            title = it
        }

        viewModel.movieUrlResults.observe(viewLifecycleOwner) { (fItem, url) ->
            if (url == null) {
                if (fItem !is FMovie) return@observe
                val intent = requireContext().createDialogIntent(
                    title = getString(R.string.play_to_browser_title, fItem.title),
                    description = getString(
                        R.string.play_to_browser_confirmation_title,
                        fItem.title
                    )
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

        viewModel.updateAdapterList.observe(viewLifecycleOwner) { (list, reset) ->
            mAdapter.run {
                if (reset) {
                    clear(); addAll(size(), list)
                    startEntranceTransition()
                } else {
                    while (remove(viewModel.movieSkeleton)) {
                        remove(viewModel.movieSkeleton)
                    }

                    addAll(size(), list)
                }
            }
        }
    }

    override fun onSearchRequestReceived(query: String) {
        if (viewModel.episodeListResults.value?.isEmpty() == false) return
        val intent = MovieSearchActivity.getIntent(requireActivity(), query)
        val bundle = makeSceneTransitionAnimation(requireActivity()).toBundle()
        activity?.startActivity(intent, bundle)
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder,
        item: Any,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        if (item is FItem) viewModel.clickItem(item)
    }

    override fun onItemSelected(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        if (item is FItem) viewModel.selectFItem(item, mAdapter.size())
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

    private fun setupRowAdapter() {
        val videoGridPresenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
        videoGridPresenter.numberOfColumns = 6

        // note: The click listeners must be called before setGridPresenter for the event listeners
        // to be properly registered on the viewholders.
        onItemViewClickedListener = this
        gridPresenter = videoGridPresenter
        gridPresenter.onItemViewSelectedListener = this

        val cardPresenterSelector = CardPresenterSelector(activity)
        mAdapter = ArrayObjectAdapter(cardPresenterSelector)
        adapter = mAdapter
        prepareEntranceTransition()
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

    private fun setupBackButtonCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = requireContext().createDialogIntent(
                    title = getString(R.string.close_app_confirmation_title),
                )
                val activityOptionsCompat = makeSceneTransitionAnimation(requireActivity())
                startForResultCloseApp.launch(intent, activityOptionsCompat)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
