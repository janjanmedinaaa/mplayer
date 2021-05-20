package medina.juanantonio.mplayer.features.media

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.widget.PlaybackControlsRow
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.extensions.createDialogIntent
import medina.juanantonio.mplayer.features.dialog.DialogFragment.Companion.ACTION_ID_POSITIVE

@AndroidEntryPoint
class VideoConsumptionFragment : VideoSupportFragment() {

    companion object {
        const val TAG = "VideoConsumption"
    }

    private lateinit var mMediaPlayerGlue: VideoMediaPlayerGlue<MediaPlayerAdapter>
    private lateinit var startForResultClosePlayer: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mMediaPlayerGlue = VideoMediaPlayerGlue<MediaPlayerAdapter>(
            activity,
            MediaPlayerAdapter(activity)
        )

        mMediaPlayerGlue.host = VideoSupportFragmentGlueHost(this)
        val audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val requestAudioFocus = audioManager.requestAudioFocus(
            { },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (requestAudioFocus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "video player cannot obtain audio focus!")
        }

        startForResultClosePlayer = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            when ("${it?.data?.data}".toLongOrNull()) {
                ACTION_ID_POSITIVE -> activity?.finish()
            }
        }

        setupBackButtonCallback()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMediaPlayerGlue.setMode(PlaybackControlsRow.RepeatAction.INDEX_NONE)
        val intentMetaData: MediaMetaData? = requireActivity().intent.getParcelableExtra(
            VideoActivity.TAG
        )
        if (intentMetaData != null) {
            mMediaPlayerGlue.title = intentMetaData.mediaTitle
            mMediaPlayerGlue.subtitle = intentMetaData.mediaArtistName
            mMediaPlayerGlue.playerAdapter.setDataSource(
                Uri.parse(intentMetaData.mediaSourcePath)
            )
        } else {
            activity?.finish()
        }
        PlaybackSeekDiskDataProvider.setDemoSeekProvider(
            mMediaPlayerGlue,
            intentMetaData?.mediaSourcePath,
            context
        )
        backgroundType = BG_DARK
    }

    override fun onResume() {
        super.onResume()
        playWhenReady(mMediaPlayerGlue)
    }

    override fun onPause() {
        mMediaPlayerGlue.pause()
        super.onPause()
    }

    private fun playWhenReady(glue: PlaybackGlue) {
        if (glue.isPrepared) {
            glue.play()
        } else {
            glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue) {
                    if (glue.isPrepared) {
                        glue.removePlayerCallback(this)
                        glue.play()
                    }
                }
            })
        }
    }

    private fun setupBackButtonCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isControlsOverlayVisible) {
                    hideControlsOverlay(true)
                } else {
                    val intent = requireContext().createDialogIntent(
                        title = getString(R.string.close_player_confirmation_title),
                    )
                    val activityOptionsCompat = makeSceneTransitionAnimation(requireActivity())
                    startForResultClosePlayer.launch(intent, activityOptionsCompat)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
