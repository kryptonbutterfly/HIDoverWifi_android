package kryptonbutterfly.hidoverwifi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Constants {
	val GSON: Gson = GsonBuilder()
		.setPrettyPrinting()
		.create()
	
	const val TRACKPAD = "TRACKPAD"
	
	const val INTERNAL_KEYSTORE_NAME = "keystore.p12"
	
	const val PROTOCOL_ID = -8596033659527291743L
	
	const val KEYBOARD_LAYOUT_PREFIX = "keyboard_"
	
	val KEYBOARD_LAYOUTS = R.raw::class.java.declaredFields
		.filter { f -> f.name.startsWith(KEYBOARD_LAYOUT_PREFIX) }
		.associate { f -> f.name.replace(KEYBOARD_LAYOUT_PREFIX, "") to f.getInt(null) }
}
