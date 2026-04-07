package kryptonbutterfly.hidoverwifi.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.widget.Toast
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_ALT
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_CTRL
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_META
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_SHIFT
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_RIGHT_ALT
import kryptonbutterfly.hidoverwifi.Constants.MAX_DELAY_MS
import kryptonbutterfly.hidoverwifi.Constants.PROTOCOL_ID
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.ToastHelper
import kryptonbutterfly.hidoverwifi.dto.Action
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.dto.InputAction
import kryptonbutterfly.hidoverwifi.prefs.DeviceSettings
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.BindException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.security.KeyStore
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

private class Connection2(
	val socket: Socket,
	val oStream: DataOutputStream,
	val iStream: DataInputStream,
	val device: DeviceSettings,
	val pressedKeys: PressedKeys = PressedKeys()
) : Closeable {
	private var lastUpdate = System.currentTimeMillis()
	private var running = false
	private var outExecutor = Executors.newSingleThreadExecutor()
	private var inExecutor = Executors.newSingleThreadExecutor()
	
	constructor(socket: Socket, device: DeviceSettings): this(
		socket,
		DataOutputStream(socket.outputStream),
		DataInputStream(socket.inputStream),
		device)
	
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
		while(running) {
			try {
				val ms = msUntilNextUpdate()
				if (ms > 0)
					Thread.sleep(ms)
				else {
					synchronized(this) {
						try {
							oStream().writeUTF(Action.KEEP_ALIVE.name)
						} catch (e: SocketException) {
							Log.v(TRACKPAD, e.message, e)
							close()
						}
					}
				}
			}
			catch (_: InterruptedException) {
			}
		}
	}
	
	override fun close() {
		running = false
		socket.close()
	}
	
	fun connect(context: Context): Connection2? {
		oStream().writeLong(PROTOCOL_ID)
		oStream().writeUTF(device.serverPassword)
		oStream().flush()
		val status = iStream.readUTF()
		when(status) {
			"ACK" -> {
				Log.v(TRACKPAD, "is connected: ${socket.isConnected}")
				running = true
				Thread(this::keepAlive).start()
				readInput(context)
				return this
			}
			"INVALID_RCON_PW" -> {
				val msg = "Invalid rcon password"
				Log.e(TRACKPAD, msg)
				ToastHelper.toast(context, msg, Toast.LENGTH_LONG)
				close()
				return null
			}
			else -> {
				val msg = "unexpected connection status: $status\n"
				Log.e(TRACKPAD, msg)
				ToastHelper.toast(context, msg, Toast.LENGTH_LONG)
				close()
				return null
			}
		}
	}
	private fun readInput(context: Context) {
		val prefs = prefs(context)
		inExecutor.execute {
			try {
				while (!socket.isClosed) {
					if (prefs.copyFromHost)
						readAction(context)
					else {
						try {
							Thread.sleep(50)
						} catch (_: InterruptedException) {}
					}
				}
			} catch (e: Throwable) { Log.e(TRACKPAD, e.message, e)}
		}
	}
	private fun readAction(context: Context) {
		try {
			val action = iStream.readUTF()
			Log.d(TRACKPAD, "in action: $action")
			when(action) {
				"CLIPBOARD" -> {
					val content = iStream.readUTF()
					val clip = ClipData.newPlainText(content, content)
					(context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)
						?.also { it.setPrimaryClip(clip) }
						?:run { Log.w(TRACKPAD, "clipboard == null")}
				}
				else -> Log.e(TRACKPAD, "Received unexpected action '$action'")
			}
		} catch (_: SocketException) { /* ignore Socket Exceptions */}
	}
	
	fun disconnect() {
		outExecutor.execute {
			try {
				Log.d(TRACKPAD, "Disconnecting from server …")
				close()
			} catch (e: Throwable) {
				Log.e(TRACKPAD, e.message, e)
			}
		}
		outExecutor.shutdown()
		inExecutor.shutdownNow()
	}
	
	fun event(context: Context, action: InputAction) {
		Log.v(TRACKPAD, "event: $action")
		val currTime = System.currentTimeMillis()
		outExecutor.execute {
			try {
				synchronized(this) {
					if (currTime + MAX_DELAY_MS > System.currentTimeMillis()) {
						Log.v(TRACKPAD, "emitting action: $action")
						action.write(oStream())
						when (action) {
							is ActionKeyboardKey -> when (action.key) {
								KEY_LEFT_SHIFT -> pressedKeys.shift = action.down
								KEY_LEFT_CTRL -> pressedKeys.ctrl = action.down
								KEY_LEFT_META -> pressedKeys.meta = action.down
								KEY_LEFT_ALT -> pressedKeys.alt = action.down
								KEY_RIGHT_ALT -> pressedKeys.altGr = action.down
								}
							else -> {}
						}
					}
				}
			} catch (e: Throwable) {
				close()
				Log.d(TRACKPAD, e.message, e)
				ToastHelper.toast(context, e.message, Toast.LENGTH_LONG)
			}
		}
	}
}

object Network {
	private val queue = LinkedBlockingQueue<Runnable>()
	private var worker = createWorker()
	private var connection: Connection2? = null
	
	private fun createWorker(): ThreadPoolExecutor {
		return ThreadPoolExecutor(1,1,0L, TimeUnit.MILLISECONDS, queue)
	}
	
	private fun ensureConnected(context: Context): Connection2? {
		return connection?.let {
			if ((it.device.bind && it.device.bindAddress.isNotEmpty() && !it.socket.isBound) ||
				it.device.address != it.socket.inetAddress.hostAddress ||
				it.device.port != it.socket.port ||
				!it.socket.isConnected ||
				it.socket.isClosed) {
				if (!it.socket.isClosed)
					it.close()
				connection = connect(context)
				connection
			} else it
		}?: run {
			connection = connect(context)
			connection
		}
	}
	private fun connect(context: Context): Connection2? {
		return prefs(context).currentDevice()?.let { device ->
			factory(context, device)?.let { factory ->
				val serverAddress = InetAddress.getByName(device.address)
				val plain = Socket()
				try {
					if (device.bind && device.bindAddress.isNotEmpty())
						plain.bind(InetSocketAddress(device.bindAddress, 0))
					
					plain.connect(InetSocketAddress(serverAddress, device.port), MAX_DELAY_MS)
					val socket = factory.createSocket(plain, device.address, device.port, true)
					Connection2(socket, device).connect(context)
				} catch (e: SocketException) {
					queue.clear()
					Log.i(TRACKPAD, e.message, e)
					ToastHelper.toast(context, e.message, Toast.LENGTH_SHORT)
					null
				} catch (e: SocketTimeoutException) {
					queue.clear()
					Log.i(TRACKPAD, e.message, e)
					val msg: String = if (device.port != 0)
							"Connection to ${device.address} (port ${device.port}) after ${MAX_DELAY_MS}ms"
						else "Connection to ${device.address} after ${MAX_DELAY_MS}ms"
					ToastHelper.toast(context, msg, Toast.LENGTH_LONG)
					null
				} catch (e: ConnectException) {
					queue.clear()
					val msg = e.cause?.message?:e.message
					Log.i(TRACKPAD, e.message, e)
					ToastHelper.toast(context, msg, Toast.LENGTH_LONG)
					null
				} catch (e: BindException) {
					queue.clear()
					Log.i(TRACKPAD, e.message, e)
					ToastHelper.toast(context, "Connect failed:\nUnable to bind to ${device.bindAddress}", Toast.LENGTH_LONG)
					null
				}
			}
		}
	}
	
	private fun factory(context: Context, device: DeviceSettings): SSLSocketFactory? {
		val file = File(context.filesDir, INTERNAL_KEYSTORE_NAME)
		if (!file.exists()) {
			val msg = "Unable to load certificate: $file"
			Log.i(TRACKPAD, msg)
			ToastHelper.toast(context, msg, Toast.LENGTH_LONG)
			return null
		}
		
		file.inputStream().use { iStream ->
			val keyStore = KeyStore.getInstance("PKCS12")
			try {
				keyStore.load(iStream, device.certPassword.toCharArray())
			} catch (e: IOException) {
				val invalidKeystoreOrPassword = "Invalid certificate or password"
				Log.i(TRACKPAD, e.message, e)
				ToastHelper.toast(context, invalidKeystoreOrPassword, Toast.LENGTH_LONG)
				return null
			}
			val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
			tmf.init(keyStore)
			val sslContext = SSLContext.getInstance("TLS")
			sslContext.init(null, tmf.trustManagers, null)
			return sslContext.socketFactory
		}
	}
	
	fun disconnect() {
		connection?.also {
			it.disconnect()
			connection = null
		}
		worker.shutdownNow()
		queue.clear()
		worker = createWorker()
	}
	
	fun event(context: Context, action: InputAction) {
		val appContext = context.applicationContext
		worker.execute {
			ensureConnected(appContext)?.also {
				try {
					it.event(appContext, action)
				} catch (e: Throwable) {
					Log.e(TRACKPAD, e.message ?: e.stackTraceToString())
					ToastHelper.toast(appContext, e.message, Toast.LENGTH_LONG)
				}
			}?:also {
				queue.clear()
			}
		}
	}
	
	fun prepareConnection(context: Context) {
		worker.execute {
			Log.i(TRACKPAD, "preparing a connection if possible.")
			ensureConnected(context)?:also {
				queue.clear()
			}
		}
	}
	
	fun pressedKeys(action: Consumer<PressedKeys>) {
		connection?.pressedKeys?.also(action::accept)
	}
}
