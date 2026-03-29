package kryptonbutterfly.hidoverwifi.prefs

import com.google.gson.annotations.Expose
import java.io.Serializable

data class DeviceSettings(
	@Expose var id: Long,
	@Expose var name: String = "",
	@Expose var address: String = "",
	@Expose var port: Int = 4620,
	@Expose var certificate: String = "",
	@Expose var certPassword: String = "public",
	@Expose var bind: Boolean = false,
	@Expose var bindAddress: String = "",
	@Expose var keepAliveInterval: Int = 15,
	@Expose var serverPassword: String = ""
	) : Serializable {
		constructor(
			prefs: Prefs,
			name: String,
			address: String,
			port: Int,
			certificate: String,
			certPassword: String,
			bind: Boolean,
			bindAddress: String,
			keepAliveInterval: Int,
			serverPassword: String
			) : this(
			prefs.genDeviceId(),
				name,
				address,
				port,
				certificate,
				certPassword,
				bind,
				bindAddress,
				keepAliveInterval,
				serverPassword)
	}
