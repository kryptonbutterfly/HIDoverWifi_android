package kryptonbutterfly.hidoverwifi

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD

private const val SHORT_DURATION = 2_000L
private const val LONG_DURATION = 3_500L
object ToastHelper {
	private abstract class Message(open val msg: String?, open val timestamp: Long) {
		abstract val toastLength: Int
		abstract val duration: Long
		
		fun toast(context: Context) {
			Handler(Looper.getMainLooper()).post {
				Toast.makeText(context, msg, toastLength).show()
			}
		}
	}
	
	private class ShortMessage(override val msg: String?, override val timestamp: Long): Message(msg, timestamp) {
		override val toastLength = Toast.LENGTH_SHORT
		override val duration = SHORT_DURATION
	}
	
	private class LongMessage(override val msg: String?, override val timestamp: Long): Message(msg, timestamp) {
		override val toastLength = Toast.LENGTH_LONG
		override val duration = LONG_DURATION
	}
	
	private var lastMessage: Message? = null
	
	private fun toastMessage(msg: Message): Boolean {
		return lastMessage?.let {
			msg.timestamp - it.timestamp > it.duration
		} ?: true
	}
	
	fun toast(context: Context, msg: String?, length: Int) {
		val currMS = System.currentTimeMillis()
		val message = when(length) {
			Toast.LENGTH_SHORT -> ShortMessage(msg, currMS)
			Toast.LENGTH_LONG -> LongMessage(msg, currMS)
			else -> { Log.w(TRACKPAD, "Unexpected Toast length $length")
				return }
		}
		if (toastMessage(message)) {
			lastMessage = message
			Handler(Looper.getMainLooper()).post { message.toast(context) }
		}
		else
			Log.d(TRACKPAD, "ignoring toast")
	}
}
