package medina.juanantonio.mplayer

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.common.extensions.mainFragment
import medina.juanantonio.mplayer.features.server.MServerListener

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().run {
            add(R.id.mainFragment, MainFragment(), MainFragment.FRAGMENT_TAG)
            commit()
        }

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is MServerListener) {
                (application as MPlayerApplication).mServer.mServerListener = fragment
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Back presses are all handled through onBackPressed.
        //
        // Note: on device, back presses emit one KEYCODE_BACK. On emulator, they
        // emit one KEYCODE_BACK **AND** one KEYCODE_DEL. We short on both to make
        // code paths consistent between the two.
        if (event.keyCode == KeyEvent.KEYCODE_BACK ||
            event.keyCode == KeyEvent.KEYCODE_DEL
        ) return super.dispatchKeyEvent(event)

        return supportFragmentManager.mainFragment().dispatchKeyEvent(event) ||
                super.dispatchKeyEvent(event)
    }
}
