package kryptonbutterfly.hidoverwifi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Constants {
	val GSON: Gson = GsonBuilder()
		.setPrettyPrinting()
		.create()
	
	const val TRACKPAD = "TRACKPAD"
}
