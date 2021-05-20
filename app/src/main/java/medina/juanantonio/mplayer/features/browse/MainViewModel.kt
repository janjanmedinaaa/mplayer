package medina.juanantonio.mplayer.features.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import medina.juanantonio.mplayer.R
import medina.juanantonio.mplayer.data.managers.FMoviesManager
import medina.juanantonio.mplayer.data.models.*
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val context: Application
) : AndroidViewModel(context) {

    lateinit var fMoviesManager: FMoviesManager
    val movieUrlResults = MutableLiveData<Pair<FItem, String?>>()
    val episodeListResults = MutableLiveData<List<FEpisode>>()
    val viewTitle = MutableLiveData(context.getString(R.string.movies_title))
    val updateAdapterList = MutableLiveData<Pair<List<FItem>, Boolean>>()

    private val movieList = arrayListOf<FItem>()
    val movieSkeleton = FMovie(
        title = "",
        imageUrl = "loading",
        videoUrl = "",
        currentPage = -1,
        nextPage = -1
    )

    var searchQuery = ""
    var selectedFItem: FItem? = null
    private var onStartDelay = false

    fun clickItem(item: FItem) {
        when (item) {
            is FMovie -> {
                if (item.currentPage != -1) {
                    fMoviesManager.selectMovie(item)
                } else return
            }
            is FSeries -> {
                fMoviesManager.selectSeries(item)
            }
            is FEpisode -> fMoviesManager.selectEpisode(item)
        }
        selectedFItem = item
    }

    fun selectFItem(item: FItem, currentAdapterSize: Int) {
        if (fMoviesManager.loadingNewMovies || movieList.isEmpty()) return

        // Checks if item is on the 2nd to the last row and
        // if there are more available pages
        if (movieList.indexOf(item) + 18 > currentAdapterSize &&
            movieList.last().currentPage != movieList.last().nextPage
        ) {

            // Prepare Skeletons
            val addSkeletons = arrayListOf<FMovie>().apply {
                for (i in 1..6) {
                    add(movieSkeleton)
                }
            }
            updateAdapterList.value = Pair(addSkeletons, false)

            // Load the next page
            loadMovieList(movieList.last().nextPage)
        }
    }

    fun stopFMoviesManager() {
        fMoviesManager.stop()
    }

    fun loadMovieList(page: Int) {
        if (searchQuery.isNotEmpty()) {
            viewTitle.value = context.getString(R.string.search_title, searchQuery)
            fMoviesManager.searchQuery(searchQuery, page)
        } else {
            viewTitle.value = context.getString(R.string.movies_title)
            fMoviesManager.browseMostWatched(page)
        }
    }

    fun handleNewMovieList(list: List<FItem>, pagination: Boolean) {
        if (pagination) addToMovieList(list)
        else updateMovieList(list)
    }

    fun handleFEpisodeList(list: List<FEpisode>, title: String? = null) {
        viewTitle.value = title ?: selectedFItem?.title
        updateAdapterList.value = Pair(list, true)
    }

    private fun updateMovieList(list: List<FItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                if (!onStartDelay) {
                    onStartDelay = true
                    delay(1000)
                }

                // Update the main movieList
                movieList.clear()
                movieList.addAll(list)

                // Update the mAdapter list separately
                updateAdapterList.value = Pair(movieList, true)
            }
        }
    }

    private fun addToMovieList(list: List<FItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                movieList.addAll(movieList.size, list)
                updateAdapterList.value = Pair(list, false)
            }
        }
    }
}