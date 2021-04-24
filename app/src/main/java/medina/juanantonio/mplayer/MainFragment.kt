package medina.juanantonio.mplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.leanback.widget.VerticalGridPresenter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import medina.juanantonio.mplayer.common.extensions.createDialogIntent
import medina.juanantonio.mplayer.data.managers.DatabaseManager
import medina.juanantonio.mplayer.data.models.VideoCard
import medina.juanantonio.mplayer.data.presenters.CardPresenterSelector
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE
import medina.juanantonio.mplayer.features.media.MediaMetaData
import medina.juanantonio.mplayer.features.media.VideoActivity
import medina.juanantonio.mplayer.features.server.MServer
import medina.juanantonio.mplayer.features.server.MServer.Companion.REQUEST_PLAY_URL
import medina.juanantonio.mplayer.features.server.MServer.Companion.SAVE_MOVIE_URL
import medina.juanantonio.mplayer.features.server.MServerListener
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment :
    VerticalGridSupportFragment(),
    OnItemViewClickedListener,
    MServerListener {

    companion object {
        const val FRAGMENT_TAG = "MainFragment"
    }

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var startForResultSaveVideo: ActivityResultLauncher<Intent>
    private lateinit var startForResultCloseApp: ActivityResultLauncher<Intent>
    private lateinit var startForResultPlayVideo: ActivityResultLauncher<Intent>
    private var requestedVideoCard: VideoCard? = null
    private var onStartDelay = false

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var mServer: MServer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        title = getString(R.string.title)
        startForResultSaveVideo = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLong()) {
                ACTION_ID_POSITIVE -> {
                    requestedVideoCard?.let { videoCard ->
                        CoroutineScope(Dispatchers.Main).launch {
                            databaseManager.addVideoCard(videoCard)
                            updateList()
                        }
                    }
                }
            }
        }

        startForResultCloseApp = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLong()) {
                ACTION_ID_POSITIVE -> activity?.finish()
            }
        }

        startForResultPlayVideo = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLong()) {
                ACTION_ID_POSITIVE -> {
                    requestedVideoCard?.let { videoCard ->
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            playVideo(videoCard)
                        }
                    }
                }
            }
        }

        setupRowAdapter()
        setupBackButtonCallback()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onRequestReceived(requestRoute: String, videoCard: VideoCard?) {
        requestedVideoCard = videoCard
        when (requestRoute) {
            SAVE_MOVIE_URL -> {
                val intent = requireContext().createDialogIntent(
                    title = getString(R.string.save_confirmation_title, videoCard?.title),
                    description = getString(
                        R.string.save_confirmation_description,
                        videoCard?.title
                    ),
                    positiveButton = getString(R.string.save)
                )
                val activityOptionsCompat = makeSceneTransitionAnimation(requireActivity())
                startForResultSaveVideo.launch(intent, activityOptionsCompat)
            }
            REQUEST_PLAY_URL -> {
                val intent = requireContext().createDialogIntent(
                    title = getString(R.string.request_play_confirmation_title),
                    positiveButton = getString(R.string.play_video),
                    negativeButton = getString(R.string.decline)
                )
                val activityOptionsCompat = makeSceneTransitionAnimation(requireActivity())
                startForResultPlayVideo.launch(intent, activityOptionsCompat)
            }
        }
    }

    private fun setupRowAdapter() {
        val videoGridPresenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
        videoGridPresenter.numberOfColumns = 4

        // note: The click listeners must be called before setGridPresenter for the event listeners
        // to be properly registered on the viewholders.
        onItemViewClickedListener = this
        gridPresenter = videoGridPresenter

        val cardPresenterSelector = CardPresenterSelector(activity)
        mAdapter = ArrayObjectAdapter(cardPresenterSelector)
        adapter = mAdapter
        prepareEntranceTransition()
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    private fun updateList() {
        CoroutineScope(Dispatchers.Main).launch {
            val videoCardList = databaseManager.getVideoCards()
            if (!onStartDelay) {
                onStartDelay = true
                delay(1000)
            }

            mAdapter.clear()
            mAdapter.addAll(mAdapter.size(), videoCardList)
            startEntranceTransition()
        }
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder,
        item: Any,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        if (item is VideoCard) {
            playVideo(item)
        }
    }

    fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_MENU) {
            return when (keyEvent.action) {
                KeyEvent.ACTION_DOWN -> {
                    Toast.makeText(context, "Menu Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> true // We swallow ACTION_UP to only handle the key event once.
            }
        }
        return false
    }

    private fun playVideo(videoCard: VideoCard) {
        val videoSources = videoCard.videoSources
        if (videoSources.isEmpty()) return

        val metaData = MediaMetaData().apply {
            mediaSourcePath = videoSources[0]
            mediaTitle = videoCard.title
            mediaArtistName = videoCard.description
            mediaAlbumArtUrl = videoCard.imageUrl
        }
        val intent = Intent(activity, VideoActivity::class.java).apply {
            putExtra(VideoActivity.TAG, metaData)
            data = Uri.parse(metaData.mediaSourcePath)
        }
        val bundle = makeSceneTransitionAnimation(requireActivity()).toBundle()
        activity?.startActivity(intent, bundle)
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
