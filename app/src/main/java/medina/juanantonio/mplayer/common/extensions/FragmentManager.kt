package medina.juanantonio.mplayer.common.extensions

import androidx.fragment.app.FragmentManager
import medina.juanantonio.mplayer.features.browse.MainFragment
import medina.juanantonio.mplayer.features.search.MovieSearchFragment

fun FragmentManager.mainFragment(): MainFragment =
    this.findFragmentByTag(MainFragment.FRAGMENT_TAG) as MainFragment

fun FragmentManager.movieSearchFragment(): MovieSearchFragment =
    this.findFragmentByTag(MovieSearchFragment.FRAGMENT_TAG) as MovieSearchFragment
