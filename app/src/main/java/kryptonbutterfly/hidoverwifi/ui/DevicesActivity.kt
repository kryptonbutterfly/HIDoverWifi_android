package kryptonbutterfly.hidoverwifi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.size
import kryptonbutterfly.hidoverwifi.Constants.DEVICE
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.network.Network
import kryptonbutterfly.hidoverwifi.prefs.DeviceSettings
import kryptonbutterfly.hidoverwifi.prefs.prefs

class DevicesActivity : AppCompatActivity() {
	private var light: Int = 0
	private var dark: Int = 0
	private val settingsResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
	private val addDeviceResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == RESULT_OK) {
			val deviceId = result.data?.getLongExtra(DEVICE, Long.MIN_VALUE) ?: Long.MIN_VALUE
			prefs(this).devices[deviceId]?.also { device ->
				buildDevice(device)
				updateUI()
			}
		}
	}
	private val editResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == RESULT_OK) {
			val deviceId = result.data?.getLongExtra(DEVICE, Long.MIN_VALUE)
			Log.i(TRACKPAD, "updating device $deviceId")
			prefs(this).devices[deviceId]?.also { prefs ->
				val devicesList = findViewById<LinearLayout>(R.id.devicesList)
				((devicesList.children.firstOrNull { deviceId == it.tag } as? ConstraintLayout)?.
				getChildAt(0) as? TextView)?.also{
					Log.i(TRACKPAD, "updating device")
					it.text = prefs.name
				}
			}
		}
	}
	private val connectResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_devices)
		
		this.light = ContextCompat.getColor(this, R.color.light)
		this.dark = ContextCompat.getColor(this, R.color.dark)
		
		buildDevices()
	}
	
	override fun onResume() {
		super.onResume()
		Network.disconnect()
	}
	
	fun onSettingsClick(@Suppress("UNUSED_PARAMETER") view: View) {
		settingsResult.launch(Intent(this, PreferencesActivity::class.java))
	}
	
	fun onAddDeviceClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val intent = Intent(this, DevicePrefsActivity::class.java)
		addDeviceResult.launch(intent)
	}
	private fun connectToDevice(deviceId: Long) {
		val prefs = prefs(this)
		if (prefs.devices[deviceId] != null) {
			prefs.currentDevice = deviceId
			val intent = Intent(this, MousePadActivity::class.java)
			connectResult.launch(intent)
		}
		else
			Log.w(TRACKPAD, "Can't connect to device with id $deviceId, device not found!")
	}
	private fun editDevice(deviceId: Long) {
		val intent = Intent(this, DevicePrefsActivity::class.java)
		intent.putExtra(DEVICE, deviceId)
		editResult.launch(intent)
	}
	private fun deleteDevice(deviceId: Long, deviceLayout: ConstraintLayout) {
		val prefs = prefs(this)
		val parent = findViewById<LinearLayout>(R.id.devicesList)
		parent.removeView(deviceLayout)
		
		if (prefs.currentDevice == deviceId)
			prefs.currentDevice = Long.MIN_VALUE
		
		prefs.devices.remove(deviceId)
		updateUI()
	}
	
	private fun buildDevices() {
		prefs(this).devices.values.forEach { buildDevice(it) }
		updateUI()
	}
	private fun buildDevice(device: DeviceSettings) {
		val layout = ConstraintLayout(applicationContext)
		layout.layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
		layout.id = View.generateViewId()
		layout.tag = device.id
		
		val name = TextView(applicationContext)
		name.id = View.generateViewId()
		val edit = ImageView(applicationContext)
		edit.id = View.generateViewId()
		val delete = ImageView(applicationContext)
		delete.id = View.generateViewId()
		
		buildDeviceName(device.id, name, edit)
		name.text = device.name
		layout.addView(name)
		
		buildEditButton(device.id, name, edit, delete)
		layout.addView(edit)
		
		buildDeleteButton(device.id, edit, delete, layout)
		layout.addView(delete)
		
		findViewById<LinearLayout>(R.id.devicesList).addView(layout)
	}
	private fun buildDeviceName(deviceId: Long, name: TextView, edit: ImageView) {
		val tmpName = findViewById<TextView>(R.id.deviceDisplayName)
		
		val params = ConstraintLayout.LayoutParams(
			tmpName.layoutParams.width,
			tmpName.layoutParams.height)
		params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
		params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
		params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
		params.endToStart = edit.id
		name.layoutParams = params
		
		name.minHeight = tmpName.minHeight
		
		name.setTextColor(tmpName.textColors)
		name.gravity = tmpName.gravity
		name.textSize = tmpName.textSize
		name.typeface = tmpName.typeface
		params.setMargins(tmpName.marginLeft, tmpName.marginTop, tmpName.marginRight, tmpName.marginBottom)
		
		name.setOnClickListener { connectToDevice(deviceId) }
	}
	private fun buildEditButton(deviceId: Long, name: TextView, edit: ImageView, delete: ImageView) {
		val tmpEd = findViewById<ImageView>(R.id.templateButtonEdit)
		
		val params = ConstraintLayout.LayoutParams(
			tmpEd.layoutParams.width,
			tmpEd.layoutParams.height)
		params.startToEnd = name.id
		params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
		params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
		params.endToStart = delete.id
		edit.layoutParams = params
		
		edit.contentDescription = tmpEd.contentDescription
		edit.minimumWidth = tmpEd.minimumWidth
		edit.minimumHeight = tmpEd.minimumHeight
		edit.setPadding(
			tmpEd.paddingLeft,
			tmpEd.paddingTop,
			tmpEd.paddingRight,
			tmpEd.paddingBottom)
		edit.setImageResource(R.drawable.edit)
		edit.setOnClickListener { editDevice(deviceId) }
	}
	private fun buildDeleteButton(deviceId: Long, edit:ImageView, delete:ImageView, layout: ConstraintLayout) {
		val tmpDel = findViewById<ImageView>(R.id.templateButtonDelete)
		
		val params = ConstraintLayout.LayoutParams(
			tmpDel.layoutParams.width,
			tmpDel.layoutParams.height)
		params.startToEnd = edit.id
		params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
		params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
		params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
		delete.layoutParams = params
		
		delete.contentDescription = tmpDel.contentDescription
		delete.minimumWidth = tmpDel.minimumWidth
		delete.setPadding(
			tmpDel.paddingLeft,
			tmpDel.paddingTop,
			tmpDel.paddingRight,
			tmpDel.paddingBottom)
		delete.setImageResource(android.R.drawable.ic_menu_delete)
		delete.setOnClickListener { deleteDevice(deviceId, layout) }
		
	}
	private fun updateUI() {
		fun colorRows(devices: LinearLayout) {
			for (i in 0 until devices.size)
				(devices[i] as ConstraintLayout).setBackgroundColor(if (i % 2 == 1) dark else light)
		}
		
		val list = findViewById<LinearLayout>(R.id.devicesList)
		colorRows(list)
	}
}
