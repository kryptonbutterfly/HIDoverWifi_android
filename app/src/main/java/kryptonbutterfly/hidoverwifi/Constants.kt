package kryptonbutterfly.hidoverwifi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Constants {
	val GSON: Gson = GsonBuilder()
		.setPrettyPrinting()
		.create()
	
	const val DEFAULT_PORT = 4620
	const val DEFAULT_KEEP_ALIVE_SECONDS = 15
	
	/**
	 * The maximum delay in milliseconds before a scheduled action will be discarded.
	 */
	const val MAX_DELAY_MS = 500
	const val TRACKPAD = "TRACKPAD"
	
	const val INTERNAL_KEYSTORE_NAME = "keystore.p12"
	
	const val PROTOCOL_ID = -8596033659527291744L
	
	const val DEVICE = "DEVICE"
	
	const val KEYBOARD_LAYOUT_PREFIX = "keyboard_"
	
	val KEYBOARD_LAYOUTS = R.raw::class.java.declaredFields
		.filter { f -> f.name.startsWith(KEYBOARD_LAYOUT_PREFIX) }
		.associate { f -> f.name.replace(KEYBOARD_LAYOUT_PREFIX, "") to f.getInt(null) }
	
	object Keys {
		const val KEY_TAB = "KEY_TAB"
		const val KEY_LEFT_SHIFT = "KEY_LEFT_SHIFT"
		const val KEY_LEFT_CTRL = "KEY_LEFT_CTRL"
		const val KEY_LEFT_META = "KEY_LEFT_META"
		const val KEY_LEFT_ALT = "KEY_LEFT_ALT"
		const val KEY_RIGHT_ALT = "KEY_RIGHT_ALT"
	}
}
