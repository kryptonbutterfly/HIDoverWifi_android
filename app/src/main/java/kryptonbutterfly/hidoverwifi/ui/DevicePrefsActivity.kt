package kryptonbutterfly.hidoverwifi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import kryptonbutterfly.hidoverwifi.Constants
import kryptonbutterfly.hidoverwifi.Constants.DEFAULT_KEEP_ALIVE_SECONDS
import kryptonbutterfly.hidoverwifi.Constants.DEFAULT_PORT
import kryptonbutterfly.hidoverwifi.Constants.DEVICE
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.prefs.DeviceSettings
import kryptonbutterfly.hidoverwifi.prefs.Prefs
import kryptonbutterfly.hidoverwifi.prefs.prefs
import kryptonbutterfly.hidoverwifi.prefs.savePrefs
import java.io.File
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Objects

class DevicePrefsActivity : AppCompatActivity() {
	
	private lateinit var addressAdapter: ArrayAdapter<AddressInfo>
	private var targetDevice: DeviceSettings? = null
	
	private lateinit var main: ConstraintLayout
	private lateinit var textDeviceName :TextInputEditText
	private lateinit var textAddress :TextInputEditText
	private lateinit var pickServerPort :NumberPicker
	private lateinit var serverPassword :TextInputEditText
	private lateinit var textCertLoc :TextView
	private lateinit var textCertPW : TextInputEditText
	private lateinit var textKeepAliveInterval :TextView
	private lateinit var bindSwitch : SwitchCompat
	private lateinit var spinnerBindAddress :Spinner
	
	private val picker =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK)
				result.data?.data?.let(this::openCertFile)
		}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_device_prefs)
		
		main = findViewById(R.id.devicePrefsMain)
		textDeviceName = findViewById(R.id.textDeviceName)
		textAddress = findViewById(R.id.textAddress)
		pickServerPort = findViewById(R.id.server_port)
		serverPassword = findViewById(R.id.server_password)
		textCertLoc = findViewById(R.id.textCertFile3)
		textCertPW = findViewById(R.id.certPwText)
		textKeepAliveInterval = findViewById(R.id.keepAlive_interval)
		bindSwitch = findViewById(R.id.switchBind2)
		spinnerBindAddress = findViewById(R.id.spinnerBindAddresses)
		
		addressAdapter = ArrayAdapter<AddressInfo>(this, android.R.layout.simple_spinner_item)
		addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		spinnerBindAddress.adapter = addressAdapter
		
		ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		targetDevice = if (intent.hasExtra(DEVICE))
			prefs(this).devices[intent.getLongExtra(DEVICE, Long.MIN_VALUE)]
		else null
		
		pickServerPort.minValue = 1
		pickServerPort.maxValue = 0xFFFF
		
		targetDevice?.also { device ->
			textDeviceName.setText(device.name)
			textAddress.setText(device.address)
			pickServerPort.value = device.port
			if (device.certificate.isNotEmpty())
				textCertLoc.text = device.certificate
			
			serverPassword.setText(device.serverPassword)
			textCertPW.setText(device.certPassword)
			textKeepAliveInterval.text = device.keepAliveInterval.toString()
			
			bindSwitch.isChecked = device.bind
		}?:also {
			pickServerPort.value = DEFAULT_PORT
			textKeepAliveInterval.text = DEFAULT_KEEP_ALIVE_SECONDS.toString()
		}
		spinnerBindAddress.visibility = if (bindSwitch.isChecked) VISIBLE else GONE
	}
	
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (!hasFocus)
			return
		
		val selected = (spinnerBindAddress.selectedItem as? AddressInfo)
		addressAdapter.clear()
		val available = getAllNetworkInterfaces()
		addressAdapter.addAll(available)
		
		selected?.also { aInfo ->
			val index =
				available.indexOfFirst { it.address.hostAddress == aInfo.address.hostAddress}
			if (index != -1)
				spinnerBindAddress.setSelection(index)
			else
				spinnerBindAddress.isSelected = false
		}?:also {
			targetDevice?.bindAddress?.also { bindAddress ->
				if (bindAddress.isNotEmpty()) {
					val index = available.indexOfFirst { it.address.hostAddress == bindAddress }
					if (index != -1)
						spinnerBindAddress.setSelection(index)
					else
						spinnerBindAddress.isSelected = false
				} else
					spinnerBindAddress.isSelected = false
			}?:also {
				spinnerBindAddress.isSelected = false
			}
		}
	}
	
	fun onCancel(@Suppress("UNUSED_PARAMETER") view: View) {
		finish()
	}
	
	fun onApply(@Suppress("UNUSED_PARAMETER") view: View) {
		val prefs = prefs(this)
		val name = textDeviceName.text.toString()
		val address = textAddress.text.toString()
		val port = pickServerPort.value
		val certificate = textCertLoc.text.toString()
		val serverPassword = serverPassword.text.toString()
		val certPassword = textCertPW.text.toString()
		val keepAliveInterval = Integer.parseInt(textKeepAliveInterval.text.toString())
		val bind = bindSwitch.isChecked
		val bindAddress = (spinnerBindAddress.selectedItem as? AddressInfo)?.address?.hostAddress ?: ""
		
		val result = Intent()
		targetDevice?.also {device ->
			device.name = name
			device.address = address
			device.port = port
			device.certificate = certificate
			device.serverPassword = serverPassword
			device.certPassword = certPassword
			device.keepAliveInterval = keepAliveInterval
			device.bind = bind
			device.bindAddress = bindAddress
			prefs.currentDevice = device.id
			result.putExtra(DEVICE, device.id)
		}?:also {
			val device = DeviceSettings(
				prefs,
				name,
				address,
				port,
				certificate,
				certPassword,
				bind,
				bindAddress,
				keepAliveInterval,
				serverPassword)
			prefs.currentDevice = device.id
			prefs.devices[device.id] = device
			result.putExtra(DEVICE, device.id)
		}
		savePrefs(this)
		setResult(RESULT_OK, result)
		finish()
	}
	fun onBindInterfaceClicked(@Suppress("UNUSED_PARAMETER") view: View) {
		spinnerBindAddress.visibility = if (bindSwitch.isChecked) VISIBLE else GONE
		main.invalidate()
	}
	
	fun onCertFileClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
		intent.addCategory(Intent.CATEGORY_OPENABLE)
		intent.type = "*/*"
		intent.putExtra(Intent.EXTRA_TITLE, "public.p12")
		picker.launch(intent)
	}
	
	private fun openCertFile(uri: Uri) {
		if (uri.scheme != "content")
			return
		
		val file = File(filesDir, INTERNAL_KEYSTORE_NAME)
		contentResolver.openInputStream(uri)?.use { iStream ->
			file.outputStream().use { iStream.copyTo(it) }
		}
		
		uri.path?.also { path ->
			val displayName = path.substring(path.indexOf(":") + 1)
			findViewById<TextView>(R.id.textCertFile3).text = displayName
		}
	}
	
	private fun getAllNetworkInterfaces(): ArrayList<AddressInfo> {
		val interfaces = NetworkInterface.getNetworkInterfaces()
		val addresses: ArrayList<AddressInfo> = ArrayList()
		interfaces.toList().stream()
			.filter { !it.isLoopback }
			.forEach { net ->
				net.inetAddresses.toList()
					.filter { it.address.size == 4 }
					.forEach { addresses.add(AddressInfo(net.displayName, it)) }
			}
		return addresses
	}
}

data class AddressInfo(val interfaceName: String , val address: InetAddress) {
	override fun toString(): String {
		return when (address) {
			is Inet4Address -> "${address.hostAddress} @$interfaceName"
			is Inet6Address -> {
				val host = Objects.toString(address.hostAddress).replace("%$interfaceName", "")
				"$host @$interfaceName"
			}
			else -> "??? $address @$interfaceName"
		}
	}
}