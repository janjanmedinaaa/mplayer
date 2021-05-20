package medina.juanantonio.mplayer.features.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.webkit.WebChromeClient
import im.delight.android.webview.AdvancedWebView

/**
 * This class serves as a WebView to be used in conjunction with a VideoEnabledWebChromeClient.
 * It makes possible:
 * - To detect the HTML5 video ended event so that the VideoEnabledWebChromeClient can exit full-screen.
 *
 * Important notes:
 * - Javascript is enabled by default and must not be disabled with getSettings().setJavaScriptEnabled(false).
 * - setWebChromeClient() must be called before any loadData(), loadDataWithBaseURL() or loadUrl() method.
 *
 * @author Cristian Perez (http://cpr.name)
 */
open class VideoEnabledWebView(context: Context, attrs: AttributeSet) : AdvancedWebView(context, attrs) {

    inner class JavascriptInterface {
        // Must match Javascript interface method of VideoEnabledWebChromeClient
        @android.webkit.JavascriptInterface
        fun notifyVideoEnd() {
            // This code is not executed in the UI thread, so we must force that to happen
            Handler(Looper.getMainLooper()).post {
                videoEnabledWebChromeClient?.onHideCustomView()
            }
        }
    }

    private var videoEnabledWebChromeClient: VideoEnabledWebChromeClient? = null
    private var addedJavascriptInterface: Boolean = false

    /**
     * Pass only a VideoEnabledWebChromeClient instance.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun setWebChromeClient(client: WebChromeClient?) {
        settings.javaScriptEnabled = true
        if (client is VideoEnabledWebChromeClient) {
            videoEnabledWebChromeClient = client
        }
        super.setWebChromeClient(client)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        addJavascriptInterface()
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        addJavascriptInterface()
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun loadUrl(url: String) {
        addJavascriptInterface()
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        addJavascriptInterface()
        super.loadUrl(url, additionalHttpHeaders)
    }

    private fun addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            addJavascriptInterface(JavascriptInterface(), "_VideoEnabledWebView") // Must match Javascript interface name of VideoEnabledWebChromeClient
            addedJavascriptInterface = true
        }
    }
}