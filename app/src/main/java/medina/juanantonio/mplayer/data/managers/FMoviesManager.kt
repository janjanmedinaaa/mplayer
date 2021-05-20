package medina.juanantonio.mplayer.data.managers

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import im.delight.android.webview.AdvancedWebView
import kotlinx.coroutines.*
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.common.JSCommands
import medina.juanantonio.mplayer.data.models.*
import medina.juanantonio.mplayer.features.webview.VideoEnabledWebChromeClient
import medina.juanantonio.mplayer.features.webview.VideoEnabledWebView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class FMoviesManager(
    activity: Activity,
    parentView: View,
    private val listener: ResultsListener? = null,
    private val showSeries: Boolean = false
) : LifecycleObserver, AdvancedWebView.Listener {

    companion object {
        private const val FMOVIES_BASE_URL = "https://fmovies.to"
        private const val STREAM_TAPE_BASE_URL = "https://streamtape.com"

        private const val SEARCH_ROUTE = "/search"
        private const val MOVIES_ROUTE = "/movies"
        private const val FILM_ROUTE = "/film"
        private const val FILTER_ROUTE = "/filter"
    }

    private var webView: VideoEnabledWebView = parentView.findViewById(R.id.webView)
    private var fItem: FItem? = null
    private var currentProgressStatus: ProgressStatus? = null
    var loadingNewMovies: Boolean = false

    private var cancellableSearchJob: Job? = null
    private var displayToBrowser = false

    init {
        val nonVideoLayout =
            parentView.findViewById<View>(R.id.nonVideoLayout)
        val videoLayout =
            parentView.findViewById<ViewGroup>(R.id.videoLayout)
        val webChromeClient = VideoEnabledWebChromeClient(
            nonVideoLayout,
            videoLayout,
            null,
            webView
        )
        webView.run {
            setListener(activity, this@FMoviesManager)
            setMixedContentAllowed(true)
            addPermittedHostname(FMOVIES_BASE_URL)
            addPermittedHostname(STREAM_TAPE_BASE_URL)
            this.webChromeClient = webChromeClient
        }
    }

    fun browseMovies(page: Int = 1) {
        val requestUrl = "$FMOVIES_BASE_URL$MOVIES_ROUTE?page=$page"
        browseQuery(requestUrl)
    }

    fun browseMostWatched(page: Int = 1) {
        val mostWatchedFilter = "?sort=views%3Adesc"
        val requestUrl = "$FMOVIES_BASE_URL$FILTER_ROUTE$mostWatchedFilter&page=$page"
        browseQuery(requestUrl)
    }

    fun searchQuery(query: String, page: Int = 1, cancellable: Boolean = false) {
        val requestUrl = "$FMOVIES_BASE_URL$SEARCH_ROUTE?keyword=$query&page=$page"
        if (cancellable) browseQueryCancellable(requestUrl)
        else browseQuery(requestUrl)
    }

    fun selectMovie(fMovie: FMovie) {
        // Return if it the user selected the same movie
        if (fMovie == fItem) return

        val requestUrl = "$FMOVIES_BASE_URL${fMovie.videoUrl}"
        this.fItem = fMovie

        webView.stopLoading()
        updateProgress(ProgressStatus.START)
        updateProgress(
            ProgressStatus.PAGE_LOOKUP,
            webView.context.getString(R.string.progress_look_up_info, fItem?.title)
        )
        webView.loadUrl(requestUrl)
    }

    fun selectEpisode(fEpisode: FEpisode) {
        val requestUrl = "$FMOVIES_BASE_URL${fEpisode.videoUrl}"
        this.fItem = fEpisode

        webView.stopLoading()
        updateProgress(ProgressStatus.START)
        updateProgress(
            ProgressStatus.PAGE_LOOKUP,
            webView.context.getString(R.string.progress_look_up_info, fItem?.title)
        )
        webView.loadUrl(requestUrl)
    }

    fun selectSeries(fSeries: FSeries) {
        val requestUrl = "$FMOVIES_BASE_URL${fSeries.videoUrl}"
        this.fItem = fSeries

        webView.stopLoading()
        updateProgress(ProgressStatus.START)
        updateProgress(
            ProgressStatus.PAGE_LOOKUP,
            webView.context.getString(R.string.progress_look_up_info, fItem?.title)
        )
        webView.loadUrl(requestUrl)
    }

    fun playFromBrowser(videoUrl: String) {
        displayToBrowser = true
        webView.stopLoading()
        webView.loadUrl("$FMOVIES_BASE_URL$videoUrl")
    }

    fun stop() {
        webView.stopLoading()
        CoroutineScope(Dispatchers.Main).launch {
            updateProgress(
                ProgressStatus.PAGE_LOOKUP,
                "Scraping cancelled..."
            )
            delay(2000)
            fItem = null
            updateProgress(ProgressStatus.DONE)
        }
    }

    private fun browseQuery(requestUrl: String) {
        if (loadingNewMovies) return
        this.loadingNewMovies = true

        CoroutineScope(Dispatchers.IO).launch {
            val document = Jsoup.connect(requestUrl).get()
            handleSearchResults(document)
        }
    }

    private fun browseQueryCancellable(requestUrl: String) {
        cancellableSearchJob?.cancel()
        cancellableSearchJob = CoroutineScope(Dispatchers.IO).launch {
            val document = Jsoup.connect(requestUrl).get()
            handleSearchResults(document)
        }
        cancellableSearchJob?.start()
    }

    private fun updateProgress(progressStatus: ProgressStatus, message: String = "") {
        currentProgressStatus = progressStatus
        listener?.onProgressChanged(progressStatus, message)
    }

    private suspend fun getWebViewHTMLDocument(): Document {
        val evaluationResult = CompletableDeferred<Document>()
        webView.evaluateJavascript(JSCommands.getHTMLDocument()) {
            val html =
                it.replace("\\u003C", "<")
                    .replace("\\", "")
            val document = Jsoup.parse(html)
            evaluationResult.complete(document)
        }
        return evaluationResult.await()
    }

    private fun getMP4Link(document: Document) {
        val iFrame =
            document.select("div#player > iframe[allow=\"autoplay; fullscreen\"]").firstOrNull()
        val iFrameLink = iFrame?.attr("src") ?: ""

        webView.stopLoading()
        updateProgress(
            ProgressStatus.PAGE_LOOKUP,
            webView.context.getString(R.string.progress_get_mp4_link, fItem?.title)
        )
        webView.loadUrl(iFrameLink)
    }

    private fun handleBrowserVideoDisplay(document: Document) {
        val autoPlayButton =
            document.select("div[data-name=\"autoplay\"] > i.fa-circle").firstOrNull()

        if (autoPlayButton != null) {
            webView.evaluateJavascript(
                JSCommands.clickAutoPlayButton()
            ) {
                displayToBrowser = true
                webView.reload()
            }
        } else {
            webView.evaluateJavascript(
                JSCommands.onlyShowIFramePlayer()
            ) {
                listener?.onWebViewPlayerReady()
            }
        }
    }

    private fun handleSearchResults(document: Document) {
        val query = document.select("div.item")
        val searchResultsMap = arrayListOf<FItem>()
        val activePage = document.select("ul.pagination > li.active").firstOrNull()
        val nextPage = activePage?.nextElementSibling()
        val disabledNextPage = nextPage != null && nextPage.className() == "disabled"

        val activePageValue = activePage?.select("span")?.html()?.toIntOrNull() ?: 1
        val nextPageValue =
            if (disabledNextPage) activePageValue
            else nextPage?.select("a")?.html()?.toIntOrNull() ?: 1

        query.forEach { queryItem ->
            val movieLink = queryItem.select("a.poster").firstOrNull()
            val moviePoster =
                queryItem.select("a.poster > img").firstOrNull()?.attr("src")
            val isMovie =
                queryItem.select("div.meta > i.type").firstOrNull()?.html() == "Movie"

            if (isMovie) {
                searchResultsMap.add(
                    FMovie(
                        title = movieLink?.attr("title") ?: return,
                        imageUrl = moviePoster ?: return,
                        videoUrl = movieLink.attr("href"),
                        currentPage = activePageValue,
                        nextPage = nextPageValue
                    )
                )
            } else if (showSeries) {
                searchResultsMap.add(
                    FSeries(
                        title = movieLink?.attr("title") ?: return,
                        imageUrl = moviePoster ?: return,
                        videoUrl = movieLink.attr("href"),
                        currentPage = activePageValue,
                        nextPage = nextPageValue
                    )
                )
            }
        }

        loadingNewMovies = false
        listener?.onMovieListReceived(searchResultsMap, activePageValue != 1)
    }

    private fun handleFilmResults(fItem: FItem, document: Document) {
        when (fItem) {
            is FMovie -> handleMovieFilm(document)
            is FSeries -> handleSeriesFilm(document)
            is FEpisode -> handleEpisodeFilm(fItem)
        }
    }

    private fun handleStreamTapeResults(document: Document) {
        val videoLink = document.select("div#videolink")
        if (videoLink.isNotEmpty()) {
            videoLink.forEach { link ->
                fItem?.let {
                    fItem = null
                    updateProgress(ProgressStatus.DONE)
                    val safeUrl = "https:${link.html()}".replace("amp;", "")
                    listener?.onMovieUrlReceived(it, safeUrl)
                }
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                fItem?.let {
                    fItem = null
                    listener?.onMovieUrlReceived(it, null)
                    updateProgress(
                        ProgressStatus.PAGE_LOOKUP,
                        "No valid Streamtape video available..."
                    )
                    delay(2000)
                    updateProgress(ProgressStatus.DONE)
                }
            }
        }
    }

    private fun handleMovieFilm(document: Document) {
        val serverList = document.select("ul.episodes > li > a")
        val streamTapeItem = serverList.firstOrNull { searchItem ->
            searchItem.select("b").html() == "Streamtape"
        }
        streamTapeItem?.run {
            if (className() == "active") {
                getMP4Link(document)
            } else {
                val streamTapePageUrl = attr("href")
                webView.loadUrl("$FMOVIES_BASE_URL$streamTapePageUrl")
            }
        }
    }

    // For Rescraping
    private fun handleSeriesFilm(document: Document) {
        val serverId = document.select("ul.servers > li").firstOrNull { li ->
            li.html() == "Streamtape"
        }?.attr("data-id")
        val seasons = document.select("ul.seasons > li")
        val seasonIds = arrayListOf<String>()
        seasons.forEach { season ->
            val dataId = season.attr("data-id")
            val dataRanges = season.attr("data-ranges")
            val ranges = dataRanges.split(",")
            ranges.forEach { range ->
                seasonIds.add("${dataId}_${serverId}_$range")
            }
        }

        val seriesListMap = arrayListOf<FEpisode>()
        seasonIds.forEach { seasonId ->
            document.select("ul#${seasonId} > li > a").forEach anchors@{ anchor ->
                val dataKName = anchor.attr("data-kname")
                if (dataKName.isBlank()) return@anchors

                val dataKNameSplit = dataKName.split(":")
                val fEpisode = FEpisode(
                    sourceDataId = seasonId,
                    season = dataKNameSplit[0],
                    episode = dataKNameSplit[1],
                    mTitle = anchor.attr("title").let {
                        if (it.isNotEmpty()) it else anchor.text()
                    },
                    videoUrl = anchor.attr("href"),
                    imageUrl = fItem?.imageUrl ?: ""
                )
                seriesListMap.add(fEpisode)
            }
        }
        fItem = null
        updateProgress(ProgressStatus.DONE)
        listener?.onEpisodeListReceived(seriesListMap)
    }

    private fun handleEpisodeFilm(fEpisode: FEpisode) {
        webView.evaluateJavascript(
            JSCommands.clickEpisodeItem(fEpisode)
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                val document = getWebViewHTMLDocument()
                getMP4Link(document)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        webView.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        webView.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        webView.onDestroy()
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        when {
            !"$url".startsWith("$FMOVIES_BASE_URL$SEARCH_ROUTE") -> {
                updateProgress(
                    ProgressStatus.PAGE_LOOKUP,
                    webView.context.getString(R.string.progress_look_up_url, url)
                )
            }
        }
    }

    override fun onPageFinished(url: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            when {
                "$url".startsWith("$FMOVIES_BASE_URL$FILM_ROUTE") -> {
                    delay(2500)
                    if (displayToBrowser) {
                        displayToBrowser = false
                        val document = getWebViewHTMLDocument()
                        handleBrowserVideoDisplay(document)
                        return@launch
                    }

                    fItem?.let {
                        updateProgress(
                            ProgressStatus.PAGE_LOOKUP,
                            webView.context.getString(
                                R.string.progress_scraping_url_for_info,
                                url,
                                fItem?.title
                            )
                        )
                        val document = getWebViewHTMLDocument()
                        handleFilmResults(it, document)
                    }
                }
                "$url".startsWith(STREAM_TAPE_BASE_URL) -> {
                    updateProgress(
                        ProgressStatus.PAGE_LOOKUP,
                        webView.context.getString(
                            R.string.progress_scraping_mp4_link,
                            fItem?.title
                        )
                    )
                    val document = getWebViewHTMLDocument()
                    handleStreamTapeResults(document)
                }
            }
        }
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {
    }

    interface ResultsListener {
        fun onMovieUrlReceived(fItem: FItem, url: String?)
        fun onMovieListReceived(list: List<FItem>, pagination: Boolean)
        fun onEpisodeListReceived(list: List<FEpisode>)
        fun onProgressChanged(progressStatus: ProgressStatus, message: String) {}
        fun onWebViewPlayerReady() {}
    }

    enum class ProgressStatus {
        START,
        PAGE_LOOKUP,
        DONE
    }
}
