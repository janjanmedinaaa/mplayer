package medina.juanantonio.mplayer

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.BoardiesITSolutions.AndroidMySQLConnector.MySQLRow
import dagger.hilt.android.AndroidEntryPoint
import medina.juanantonio.mplayer.common.extensions.mainFragment
import medina.juanantonio.mplayer.data.managers.MySQLManager
import medina.juanantonio.mplayer.features.server.MServerListener
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MySQLManager.ConnectionListener {

    private var mServerListener: MServerListener? = null

    @Inject
    lateinit var mySQLManager: MySQLManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(mySQLManager)
        mySQLManager.connectionListener = this

        supportFragmentManager.beginTransaction().run {
            add(R.id.mainFragment, MainFragment(), MainFragment.FRAGMENT_TAG)
            commit()
        }

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is MServerListener) {
                mServerListener = fragment
                (application as MPlayerApplication).mServer.mServerListener = mServerListener
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mServerListener?.let {
            (application as MPlayerApplication).mServer.mServerListener = it
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

    override fun onConnect() {
        mySQLManager.executeQuery("SELECT * FROM movies") { resultSet, exception ->
            if (exception != null) return@executeQuery

            var row: MySQLRow?
            while (resultSet?.nextRow.also { row = it } != null) {

            }
        }
    }
}
