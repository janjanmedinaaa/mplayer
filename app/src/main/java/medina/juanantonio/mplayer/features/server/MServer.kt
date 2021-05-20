package medina.juanantonio.mplayer.features.server

import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MServer(port: Int) : NanoHTTPD(port) {

    var mServerListener: MServerListener? = null

    override fun serve(session: IHTTPSession?): Response {
        return when {
            session?.method == Method.GET
                    && session.uri == "/" -> {

                CoroutineScope(Dispatchers.Main).launch {
                    session.parms["search"]?.let {
                        mServerListener?.onSearchRequestReceived(it)
                    }
                }

                newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_PLAINTEXT,
                    "Thanks for using MPlayer."
                )
            }
            else -> {
                newFixedLengthResponse(
                    "<html><body><h1>MPlayer Server Running</h1></body></html>"
                )
            }
        }
    }

    interface MServerListener {
        fun onSearchRequestReceived(query: String)
    }
}