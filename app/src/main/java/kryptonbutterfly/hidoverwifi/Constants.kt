package kryptonbutterfly.hidoverwifi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Constants {
	val GSON: Gson = GsonBuilder()
		.setPrettyPrinting()
		.create()
	
	const val TRACKPAD = "TRACKPAD"
	
	const val KEYSTORE_ALIAS = "HIDoverWifi"
	
	const val INTERNAL_KEYSTORE_NAME = "keystore.p12"
}
