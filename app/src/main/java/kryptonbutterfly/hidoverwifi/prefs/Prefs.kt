package kryptonbutterfly.hidoverwifi.prefs

import android.content.Context
import android.util.Log
import com.google.gson.annotations.Expose
import kryptonbutterfly.hidoverwifi.Constants.GSON
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.prefs.PrefsHelper.isInitialized
import java.io.File
import java.util.Objects

private const val PREFS_FILE: String = "Settings.json"

data class Prefs(
	@Expose var showScrollBar:Boolean = false,
	@Expose var keyboardLayout: String = "",
	@Expose var copyFromHost: Boolean = false,
	@Expose var deviceIdSource: Long = Long.MIN_VALUE,
	@Expose var devices: HashMap<Long, DeviceSettings> = HashMap(),
	@Expose var currentDevice: Long = Long.MIN_VALUE
) {
	fun genDeviceId(): Long {
		return deviceIdSource++
	}
	
	fun currentDevice(): DeviceSettings? {
		return devices[currentDevice]
	}
}

private object PrefsHelper {
	lateinit var prefs: Prefs
	
	private var lastData: String = ""
	
	fun load(context: Context) {
		val file = File(context.filesDir, PREFS_FILE)
		if (file.exists()) {
			val json = file.bufferedReader().use { it.readText() }
			lastData = json
			prefs = GSON.fromJson(json, Prefs::class.java)
		}
		else {
			lastData = ""
			prefs = Prefs()
		}
	}
	fun save(context: Context) {
		Log.d(TRACKPAD, "save prefs initiated")
		if (!isInitialized()) {
			Log.d(TRACKPAD, "No data to store -- SKIPPING")
			return
		}
		val file = File(context.filesDir, PREFS_FILE)
		file.parentFile?.also { parent ->
			if (!parent.exists())
				parent.mkdirs()
		}
		val json = GSON.toJson(prefs)
		if (file.exists() && Objects.equals(lastData, json)) {
			Log.d(TRACKPAD, "Prefs weren't changed -- SKIPPING")
			return
		}
		Log.d(TRACKPAD, "Persisting prefs in $file")
		lastData = json
		file.bufferedWriter().use { it.write(json) }
	}
	
	fun isInitialized(): Boolean {
		return ::prefs.isInitialized
	}
}

fun savePrefs(context: Context) {
	PrefsHelper.save(context)
}


fun prefs(context: Context): Prefs {
	if (!isInitialized())
		PrefsHelper.load(context)
	return PrefsHelper.prefs
}
