package medina.juanantonio.mplayer.common.extensions

import androidx.fragment.app.FragmentManager
import medina.juanantonio.mplayer.MainFragment

fun FragmentManager.mainFragment(): MainFragment =
    this.findFragmentByTag(MainFragment.FRAGMENT_TAG) as MainFragment
