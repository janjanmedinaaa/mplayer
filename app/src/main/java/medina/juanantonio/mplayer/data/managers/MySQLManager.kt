package medina.juanantonio.mplayer.data.managers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.BoardiesITSolutions.AndroidMySQLConnector.Connection
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.InvalidSQLPacketException
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLConnException
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLException
import com.BoardiesITSolutions.AndroidMySQLConnector.IConnectionInterface
import com.BoardiesITSolutions.AndroidMySQLConnector.IResultInterface
import com.BoardiesITSolutions.AndroidMySQLConnector.ResultSet
import java.io.IOException
import java.lang.Exception

class MySQLManager(
    private val hostname: String,
    private val username: String,
    private val password: String,
    private val port: Int,
    private val database: String
): IConnectionInterface, LifecycleObserver {

    private var isConnected = false
    private var mySQLConnection: Connection? = null
    var connectionListener: ConnectionListener? = null

    override fun actionCompleted() {
        isConnected = true
        connectionListener?.onConnect()
    }

    override fun handleInvalidSQLPacketException(ex: InvalidSQLPacketException?) {
        isConnected = false
        mySQLConnection = null
        connectionListener?.onException(ex)
    }

    override fun handleMySQLException(ex: MySQLException?) {
        isConnected = false
        mySQLConnection = null
        connectionListener?.onException(ex)
    }

    override fun handleIOException(ex: IOException?) {
        isConnected = false
        mySQLConnection = null
        connectionListener?.onException(ex)
    }

    override fun handleMySQLConnException(ex: MySQLConnException?) {
        isConnected = false
        mySQLConnection = null
        connectionListener?.onException(ex)
    }

    override fun handleException(exception: Exception?) {
        isConnected = false
        mySQLConnection = null
        connectionListener?.onException(exception)
    }

    fun executeQuery(query: String, onResult: (ResultSet?, Exception?) -> Unit) {
        if (!isConnected) {
            onResult(null, Exception("MySQLConnection is Closed."))
            return
        }

        val statement = mySQLConnection?.createStatement()
        statement?.executeQuery(query, object : IResultInterface {
            override fun executionComplete(resultSet: ResultSet?) {
                onResult(resultSet, null)
            }

            override fun handleInvalidSQLPacketException(ex: InvalidSQLPacketException?) {
                onResult(null, ex)
            }

            override fun handleMySQLException(ex: MySQLException?) {
                onResult(null, ex)
            }

            override fun handleIOException(ex: IOException?) {
                onResult(null, ex)
            }

            override fun handleMySQLConnException(ex: MySQLConnException?) {
                onResult(null, ex)
            }

            override fun handleException(ex: Exception?) {
                onResult(null, ex)
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun closeConnection() {
        mySQLConnection?.close()
        mySQLConnection = null
        isConnected = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun reCreateConnection() {
        if (isConnected) return

        mySQLConnection?.close()
        mySQLConnection = Connection(
            hostname,
            username,
            password,
            port,
            database,
            this
        )
    }

    interface ConnectionListener {
        fun onConnect()
        fun onException(exception: Exception?) {}
    }
}