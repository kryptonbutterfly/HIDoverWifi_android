package kryptonbutterfly.hidoverwifi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import kryptonbutterfly.hidoverwifi.Constants.INTERNAL_KEYSTORE_NAME
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.io.File
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Objects

class PreferencesActivity : AppCompatActivity() {
	
	private lateinit var adapter: ArrayAdapter<AddressInfo>
	private lateinit var main: ConstraintLayout
	private lateinit var bindSwitch: SwitchCompat
	private lateinit var spinnerBindAddress: Spinner
	
	private val picker =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK)
				result.data?.data?.let(this::openCertFile)
		}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_preferences)
		main = findViewById(R.id.keyboardTopBar)
		ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		val prefs = prefs(this)
		
		val switchScrollBar = findViewById<SwitchCompat>(R.id.switchScrollBar)
		val textAddress = findViewById<TextInputEditText>(R.id.textAddress)
		val pickerServerPort = findViewById<NumberPicker>(R.id.server_port)
		val textCertLoc = findViewById<TextView>(R.id.textCertFile)
		val textCertPW = findViewById<TextInputEditText>(R.id.certPwText)
		bindSwitch = findViewById(R.id.switchBind)
		spinnerBindAddress = findViewById(R.id.spinnerBindAddresses)
		switchScrollBar.isChecked = prefs.showScrollBar
		textAddress.setText(prefs.address)
		pickerServerPort.minValue = 1
		pickerServerPort.maxValue = 0xFFFF
		pickerServerPort.value = prefs.port
		if (prefs.certificate.isNotEmpty())
			textCertLoc.text = prefs.certificate
		
		textCertPW.setText(prefs.certPassword)
		bindSwitch.isChecked = prefs.bind
		spinnerBindAddress.visibility = if (prefs.bind) VISIBLE else GONE
		
		adapter = ArrayAdapter<AddressInfo>(this, android.R.layout.simple_spinner_item)
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		spinnerBindAddress.adapter = adapter
		
		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				prefs.showScrollBar = switchScrollBar.isChecked
				prefs.address = textAddress.text.toString()
				prefs.port = pickerServerPort.value
				prefs.certificate = textCertLoc.text.toString()
				prefs.certPassword = textCertPW.text.toString()
				prefs.bind = bindSwitch.isChecked
				prefs.bindAddress =
					(spinnerBindAddress.selectedItem as? AddressInfo)?.address?.hostAddress ?: ""
				finish()
			}
		})
	}
	
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (!hasFocus)
			return
		
		val selected = (spinnerBindAddress.selectedItem as? AddressInfo)
		adapter.clear()
		val available = getAllNetworkInterfaces()
		adapter.addAll(available)
		
		selected?.also { aInfo ->
			val index =
				available.indexOfFirst { it.address.hostAddress == aInfo.address.hostAddress }
			if (index != -1)
				spinnerBindAddress.setSelection(index)
			else
				spinnerBindAddress.isSelected = false
		} ?: also {
			val bindAddress = prefs(this).bindAddress
			if (bindAddress.isNotEmpty()) {
				val index = available.indexOfFirst { it.address.hostAddress == bindAddress }
				if (index != -1)
					spinnerBindAddress.setSelection(index)
				else
					spinnerBindAddress.isSelected = false
			} else
				spinnerBindAddress.isSelected = false
		}
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
			findViewById<TextView>(R.id.textCertFile).text = displayName
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

private data class AddressInfo(val interfaceName: String, val address: InetAddress) {
	override fun toString(): String {
		return when (address) {
			is Inet4Address -> "${address.hostAddress} @$interfaceName"
			is Inet6Address -> "%s @%s".format(
				Objects.toString(address.hostAddress).replace("%$interfaceName", ""), interfaceName
			)
			
			else -> "??? $address @$interfaceName"
		}
	}
}
