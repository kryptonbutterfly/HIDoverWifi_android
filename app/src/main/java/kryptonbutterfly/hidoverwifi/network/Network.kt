package kryptonbutterfly.hidoverwifi.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.Constants.MAX_DELAY_MS
import kryptonbutterfly.hidoverwifi.Constants.PROTOCOL_ID
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.dto.Action
import kryptonbutterfly.hidoverwifi.dto.InputAction
import kryptonbutterfly.hidoverwifi.prefs.DeviceSettings
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.io.Closeable
import java.io.DataInputStream
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

private class Connection(
	val socket: Socket,
	val oStream: DataOutputStream,
	val iStream: DataInputStream,
	val device: DeviceSettings
) :
	Closeable {
	private var lastUpdate = System.currentTimeMillis()
	private var running = true
	private fun update() {
		lastUpdate = System.currentTimeMillis()
	}
	
	private fun msUntilNextUpdate(): Long {
		return lastUpdate + 1000 * device.keepAliveInterval - System.currentTimeMillis()
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
	private var outExecutor: ExecutorService? = null
	private var inExecutor: ExecutorService? = null
	
	private fun factory(context: ContextWrapper, device: DeviceSettings): SSLSocketFactory? {
		val file = File(context.filesDir, INTERNAL_KEYSTORE_NAME)
		if (!file.exists())
			return null
		return file.inputStream().use { iStream ->
			val keyStore = KeyStore.getInstance("PKCS12")
			keyStore.load(iStream, device.certPassword.toCharArray())
			val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
			tmf.init(keyStore)
			val context = SSLContext.getInstance("TLS")
			context.init(null, tmf.trustManagers, null)
			context.socketFactory
		}
	}
	
	private fun connect(context: ContextWrapper) {
		connection = null
		prefs(context).currentDevice()?.also { device ->
			val pwBytes = device.serverPassword.toByteArray(UTF_8)
			val buffer = ByteBuffer.allocate(8 + 1 + pwBytes.size)
			buffer.putLong(PROTOCOL_ID)
			buffer.put(pwBytes.size.toByte())
			buffer.put(pwBytes)
			
			factory(context, device)?.also { factory ->
				val serverAddress = InetAddress.getByName(device.address)
				val socket: Socket
				if (device.bind && device.bindAddress.isNotEmpty()) {
					val bindAddress = InetAddress.getByName(device.bindAddress)
					socket = factory.createSocket(serverAddress, device.port, bindAddress, 0)
				} else {
					socket = factory.createSocket(serverAddress, device.port)
				}
				
				socket.outputStream.write(buffer.array())
				socket.outputStream.flush()
				Log.v(TRACKPAD, "is connected: ${socket.isConnected}")
				connection = Connection(
					socket,
					DataOutputStream(socket.outputStream),
					DataInputStream(socket.inputStream),
					device)
				connection?.keepAlive()
			}
		}
	}
	
	private fun ensureConnected(context: ContextWrapper) {
		connection?.also {
			if ((it.device.bind && it.device.bindAddress.isNotEmpty() && !it.socket.isBound) ||
				it.device.address != it.socket.inetAddress.hostAddress ||
				it.device.port != it.socket.port ||
				!it.socket.isConnected ||
				it.socket.isClosed) {
				if (!it.socket.isClosed)
					it.close()
				connect(context)
			}
		} ?: also { connect(context) }
	}
	
	fun disconnect(terminate: Boolean = false) {
		Log.d(TRACKPAD, "Network#disconnect(terminate = $terminate) invoked.")
		inExecutor?.also { exec ->
			if (terminate) {
				try {
					exec.shutdownNow()
				} catch (e: Throwable) {
					Log.e(TRACKPAD, e.stackTraceToString(), e)
				}
			}
		}
		inExecutor = null
		
		outExecutor?.also { exec ->
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
		outExecutor = null
	}
	
	fun ensureConnection(context: ContextWrapper) {
		Log.i(TRACKPAD, "Ensuring a connection is established.")
		event(context, null)
	}
	
	fun event(context: ContextWrapper, action: InputAction?) {
		outExecutor = outExecutor ?: Executors.newSingleThreadExecutor()
		val currTime = System.currentTimeMillis()
		outExecutor?.execute {
			try {
				ensureConnected(context)
				action?.also { action ->
					connection?.also {
						Log.v(TRACKPAD, "event: $action")
						try {
							synchronized(it) {
								if (currTime + MAX_DELAY_MS > System.currentTimeMillis()) {
									action.write(it.oStream())
								}
							}
						} catch (_: Throwable) {
							it.close()
							connection = null
						}
					}
				}
			} catch (e: Throwable) {
				Log.e(TRACKPAD, e.stackTraceToString(), e)
				Log.d(TRACKPAD, prefs(context).toString())
			}
		}
		
		if (prefs(context).copyFromHost) {
			inExecutor = inExecutor ?: Executors.newSingleThreadExecutor()
			inExecutor?.execute {
				try {
					connection?.also { conn ->
						while (!conn.socket.isClosed)
							readAction(context, conn)
					}
				} catch (e: Throwable) {
					Log.e(TRACKPAD, e.stackTraceToString(), e)
				}
			}
		}
	}
	
	private fun readAction(context: ContextWrapper, conn: Connection) {
		try {
			val action = conn.iStream.readUTF()
			when (action) {
				"CLIPBOARD" -> {
					val content = conn.iStream.readUTF()
					val clip = ClipData.newPlainText(content, content)
					(context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
						?.also {
							it.setPrimaryClip(clip)
						}
						?: run { Log.w(TRACKPAD, "clipboard == null") }
				}
				
				else -> Log.e(TRACKPAD, "Received unexpected action '$action'")
			}
		} catch (_: SocketException) { /* ignore Socket Exceptions */
		}
	}
}
