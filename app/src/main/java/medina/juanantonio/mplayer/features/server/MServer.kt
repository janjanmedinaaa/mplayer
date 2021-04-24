package medina.juanantonio.mplayer.features.server

import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import medina.juanantonio.mplayer.data.managers.DatabaseManager
import medina.juanantonio.mplayer.data.models.VideoCard

class MServer(port: Int) : NanoHTTPD(port) {

    companion object {
        const val SAVE_MOVIE_URL = "/saveMovie"
        const val REQUEST_PLAY_URL = "/requestPlay"
    }

    var mServerListener: MServerListener? = null

    override fun serve(session: IHTTPSession?): Response {
        return when {
            session?.method == Method.POST
                    && session.uri == SAVE_MOVIE_URL -> {

                session.parseBody(HashMap<String, String>())
                CoroutineScope(Dispatchers.Main).launch {
                    session.parms?.run {
                        if (isEmpty()) return@run

                        val videoCard = VideoCard().apply {
                            title = get("title")
                            description = get("description")
                            videoSources = listOf(get("source"))
                            imageUrl = get("imageUrl")
                        }

                        mServerListener?.onRequestReceived(session.uri, videoCard)
                    }
                }

                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_PLAINTEXT,
                    "Movie Save Request Sent!"
                )
            }
            session?.method == Method.POST
                    && session.uri == REQUEST_PLAY_URL -> {

                session.parseBody(HashMap<String, String>())
                CoroutineScope(Dispatchers.Main).launch {
                    session.parms?.run {
                        if (isEmpty()) return@run

                        val videoCard = VideoCard().apply {
                            videoSources = listOf(get("source"))
                        }

                        mServerListener?.onRequestReceived(session.uri, videoCard)
                    }
                }

                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_PLAINTEXT,
                    "Movie Play Request Sent!"
                )
            }
            else -> {
                newFixedLengthResponse(
                    "<html><body><h1>MPlayer Server Running</h1></body></html>"
                )
            }
        }
    }
}

interface MServerListener {
    fun onRequestReceived(requestRoute: String, videoCard: VideoCard?)
}