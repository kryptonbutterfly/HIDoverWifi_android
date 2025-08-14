package kryptonbutterfly.hidoverwifi.prefs

import android.content.ContextWrapper
import com.google.gson.annotations.Expose
import kryptonbutterfly.hidoverwifi.Constants.GSON
import java.io.File

private const val PREFS_FILE: String = "Settings.json"

data class Prefs(
	@Expose var showScrollBar:Boolean = false,
	@Expose var address: String = "",
	@Expose var port: Int = 4620,
	@Expose var certificate: String = "",
	@Expose var certPassword: String = "public",
	@Expose var bind: Boolean = false,
	@Expose var bindAddress: String = "",
	@Expose var keepAliveInterval: Int = 15,
	@Expose var serverPassword: String = "",
	@Expose var keyboardLayout: String = "",
	@Expose var copyFromHost: Boolean = false
) {
	fun save(context: ContextWrapper) {
		val json = GSON.toJson(this)
		val file = File(context.filesDir, PREFS_FILE)
		file.parentFile?.also { parent ->
			if (!parent.exists())
				parent.mkdirs()
		}
		file.bufferedWriter().use { it.write(json) }
	}
}

private object PrefsHelper {
	lateinit var prefs: Prefs
	
	fun load(context: ContextWrapper) {
		val file = File(context.filesDir, PREFS_FILE)
		if (file.exists()) {
			val json = file.bufferedReader().use { it.readText() }
			prefs = GSON.fromJson(json, Prefs::class.java)
		}
		else {
			prefs = Prefs()
		}
	}
	
	fun isInitialized(): Boolean {
		return ::prefs.isInitialized
	}
}

fun prefs(context: ContextWrapper): Prefs {
	if (!PrefsHelper.isInitialized())
		PrefsHelper.load(context)
	return PrefsHelper.prefs
}
