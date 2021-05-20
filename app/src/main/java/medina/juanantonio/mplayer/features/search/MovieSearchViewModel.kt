package medina.juanantonio.mplayer.features.search

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import medina.juanantonio.mplayer.data.models.FItem
import medina.juanantonio.mplayer.features.browse.MainViewModel
import javax.inject.Inject

@HiltViewModel
class MovieSearchViewModel @Inject constructor(
    context: Application
) : MainViewModel(context) {

    fun searchQuery(query: String) {
        fMoviesManager.searchQuery(query, page = 1, cancellable = true)
    }

    fun handleSearchResults(list: List<FItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                updateAdapterList.value = Pair(list, true)
            }
        }
    }
}