package kryptonbutterfly.hidoverwifi.network

import android.content.ContextWrapper
import android.util.Log
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.dto.InputAction
import kryptonbutterfly.hidoverwifi.prefs.Prefs
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

private data class Connection(val socket: Socket, val oStream: DataOutputStream)

object Network {
	private var connection: Connection? = null
	private var executor: ExecutorService? = null
	
	private fun factory(context: ContextWrapper, prefs: Prefs): SSLSocketFactory? {
		
		val file = File(context.filesDir, INTERNAL_KEYSTORE_NAME)
		if (file.exists())
			file.inputStream().use { iStream ->
				val keyStore = KeyStore.getInstance("PKCS12")
				keyStore.load(iStream, prefs.certPassword.toCharArray())
				val tmf =
					TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
				tmf.init(keyStore)
				val context = SSLContext.getInstance("TLS")
				context.init(null, tmf.trustManagers, null)
				return context.socketFactory
			}
		return null
	}
	
	private fun connect(context: ContextWrapper) {
		val prefs = prefs(context)
		factory(context, prefs)?.also { factory ->
			val serverAddress = InetAddress.getByName(prefs.address)
			val socket: Socket
			if (prefs.bind && prefs.bindAddress.isNotEmpty()) {
				val bindAddress = InetAddress.getByName(prefs.bindAddress)
				socket = factory.createSocket(serverAddress, prefs.port, bindAddress, 0)
			} else {
				socket = factory.createSocket(serverAddress, prefs.port)
			}
			socket.soTimeout = 250
			Log.v(TRACKPAD, "is connected: ${socket.isConnected}")
			connection = Connection(socket, DataOutputStream(socket.outputStream))
		}
	}
	
	private fun ensureConnected(context: ContextWrapper) {
		connection?.also {
			val prefs = prefs(context)
			var requireNew = false
			if (prefs.bind && prefs.bindAddress.isNotEmpty() && !it.socket.isBound)
				requireNew = true
			if (prefs.address != it.socket.inetAddress.hostAddress)
				requireNew = true
			if (prefs.port != it.socket.port)
				requireNew = true
			if (!it.socket.isConnected)
				requireNew = true
			if (it.socket.isClosed)
				requireNew = true
			
			if (requireNew) {
				if (!it.socket.isClosed)
					it.socket.close()
				connect(context)
			}
		} ?: also { connect(context) }
	}
	
	fun disconnect(terminate: Boolean = false) {
		executor?.also { exec ->
			exec.execute {
				try {
					connection?.also {
						it.socket.close()
						connection = null
					}
				} catch (e: Throwable) {
					Log.e(TRACKPAD, e.stackTraceToString(), e)
				}
			}
			if (terminate)
				exec.shutdown()
		}
		executor = null
	}
	
	fun event(context: ContextWrapper, action: InputAction) {
		executor = executor ?: Executors.newSingleThreadExecutor()
		executor?.execute {
			try {
				ensureConnected(context)
				connection?.also {
					Log.v(TRACKPAD, "event: $action")
					try {
						action.write(it.oStream)
					} catch (_: Throwable) {
						it.socket.close()
						connection = null
					}
				}
			} catch (e: Throwable) {
				Log.e(TRACKPAD, e.stackTraceToString(), e)
				Log.d(TRACKPAD, prefs(context).toString())
				
			}
		}
	}
}
