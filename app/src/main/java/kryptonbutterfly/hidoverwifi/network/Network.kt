package kryptonbutterfly.hidoverwifi.network

import android.content.ContextWrapper
import android.util.Log
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.Constants.PROTOCOL_ID
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.dto.Action
import kryptonbutterfly.hidoverwifi.dto.InputAction
import kryptonbutterfly.hidoverwifi.prefs.Prefs
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.io.Closeable
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.security.KeyStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.text.Charsets.UTF_8

private class Connection(val socket: Socket, val oStream: DataOutputStream, val prefs: Prefs) :
	Closeable {
	private var lastUpdate = System.currentTimeMillis()
	private var running = true
	private fun update() {
		lastUpdate = System.currentTimeMillis()
	}
	
	private fun msUntilNextUpdate(): Long {
		return (lastUpdate + 1000 * prefs.keepAliveInterval) - System.currentTimeMillis()
	}
	
	fun oStream(): DataOutputStream {
		update()
		return oStream
	}
	
	fun keepAlive() {
		Thread {
			while (running) {
				try {
					val ms = msUntilNextUpdate()
					if (ms > 0)
						Thread.sleep(ms)
					else {
						synchronized(this) {
							try {
								oStream().writeUTF(Action.KEEP_ALIVE.name)
							} catch (_: SocketException) {
								close()
							}
						}
					}
				} catch (_: InterruptedException) {
				}
			}
		}.start()
	}
	
	override fun close() {
		running = false
		socket.close()
	}
}

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
		
		val pwBytes = prefs.serverPassword.toByteArray(UTF_8)
		val buffer = ByteBuffer.allocate(8 + 1 + pwBytes.size)
		buffer.putLong(PROTOCOL_ID)
		buffer.put(pwBytes.size.toByte())
		buffer.put(pwBytes)
		
		factory(context, prefs)?.also { factory ->
			val serverAddress = InetAddress.getByName(prefs.address)
			val socket: Socket
			if (prefs.bind && prefs.bindAddress.isNotEmpty()) {
				val bindAddress = InetAddress.getByName(prefs.bindAddress)
				socket = factory.createSocket(serverAddress, prefs.port, bindAddress, 0)
			} else {
				socket = factory.createSocket(serverAddress, prefs.port)
			}
			socket.outputStream.write(buffer.array())
			socket.outputStream.flush()
			Log.v(TRACKPAD, "is connected: ${socket.isConnected}")
			connection = Connection(socket, DataOutputStream(socket.outputStream), prefs)
			connection?.keepAlive()
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
					it.close()
				connect(context)
			}
		} ?: also { connect(context) }
	}
	
	fun disconnect(terminate: Boolean = false) {
		Log.d(TRACKPAD, "Network#disconnect(terminate = $terminate) invoked.")
		executor?.also { exec ->
			exec.execute {
				try {
					connection?.also {
						Log.d(TRACKPAD, "Disconnecting from server…")
						it.close()
						connection = null
					}
				} catch (e: Throwable) {
					Log.e(TRACKPAD, e.stackTraceToString(), e)
				}
			}
			if (terminate) {
				Log.d(TRACKPAD, "Stopping network executor service.")
				exec.shutdown()
			}
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
						synchronized(it) {
							action.write(it.oStream())
						}
					} catch (_: Throwable) {
						it.close()
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
