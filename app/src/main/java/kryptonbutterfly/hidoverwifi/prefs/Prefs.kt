package kryptonbutterfly.hidoverwifi.prefs

import android.content.ContextWrapper
import com.google.gson.annotations.Expose
import kryptonbutterfly.hidoverwifi.Constants.GSON
import java.io.File

private const val PREFS_FILE: String = "Settings.json"

data class Prefs(
	@Expose var showScrollBar:Boolean = false,
	@Expose var keyboardLayout: String = "",
	@Expose var copyFromHost: Boolean = false,
	@Expose var deviceIdSource: Long = Long.MIN_VALUE,
	@Expose var devices: HashMap<Long, DeviceSettings> = HashMap(),
	@Expose var currentDevice: Long = Long.MIN_VALUE
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
	
	fun genDeviceId(): Long {
		return deviceIdSource++
	}
	
	fun currentDevice(): DeviceSettings? {
		return devices[currentDevice]
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
