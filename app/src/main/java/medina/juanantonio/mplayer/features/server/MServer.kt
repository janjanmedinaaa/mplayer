package medina.juanantonio.mplayer.features.server

import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import medina.juanantonio.mplayer.data.managers.DatabaseManager
import medina.juanantonio.mplayer.data.models.VideoCard

class MServer(
    port: Int,
    private val databaseManager: DatabaseManager
) : NanoHTTPD(port) {

    companion object {
        const val SAVE_MOVIE_URL = "/saveMovie"
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

                        mServerListener?.onRequestReceived(videoCard)
                    }
                }

                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_PLAINTEXT,
                    "Movie Save Request Sent!"
                )
            }
            else -> {
                newFixedLengthResponse(
                    "<html><body><h1>MPlayer Server Running</h1></body></html>"
                )
            }
        }
    }

    fun saveVideoCard(videoCard: VideoCard, onSaved: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            databaseManager.addVideoCard(videoCard)
            onSaved()
        }
    }
}

interface MServerListener {
    fun onRequestReceived(videoCard: VideoCard?)
}