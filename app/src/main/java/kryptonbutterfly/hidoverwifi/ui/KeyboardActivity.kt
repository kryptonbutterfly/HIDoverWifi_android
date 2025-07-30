package kryptonbutterfly.hidoverwifi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kryptonbutterfly.hidoverwifi.Constants.GSON
import kryptonbutterfly.hidoverwifi.Constants.KEYBOARD_LAYOUTS
import kryptonbutterfly.hidoverwifi.Constants.KEYBOARD_LAYOUT_PREFIX
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.KeyText
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardType
import kryptonbutterfly.hidoverwifi.dto.ActionTextTyped
import kryptonbutterfly.hidoverwifi.network.Network
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.util.Arrays

class KeyboardActivity : AppCompatActivity() {
	private val settingsResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			loadKeyboardLayout()
			updateKeyText()
		}
	
	private val keyboardLayout: HashMap<Int, KeyText?> = HashMap()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_keyboard)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keyboardTopBar)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		loadKeyboardLayout()
		updateKeyText()
	}
	
	override fun onStop() {
		Network.disconnect(true)
		super.onStop()
	}
	
	private fun loadKeyboardLayout() {
		val targetName = prefs(this).keyboardLayout
		KEYBOARD_LAYOUTS.getOrElse(targetName) { KEYBOARD_LAYOUTS.values.firstOrNull() }
			?.also { layoutId ->
			val json = resources.openRawResource(layoutId)
				.bufferedReader()
				.use { it.readText() }
			GSON.fromJson<HashMap<String, ArrayList<String>>>(json, HashMap::class.java)
				.forEach { name, data ->
					val id = resources.getIdentifier(name, "id", packageName)
					val keyText: KeyText? = when (val size = data.size) {
						0 -> null
						2 -> KeyText(data[0], data[1])
						3 -> KeyText(data[0], data[1], data[2])
						4 -> KeyText(data[0], data[1], data[2], data[3])
						5 -> KeyText(data[0], data[1], data[2], data[3], data[4])
						else -> {
							if (size < 2)
								throw IllegalArgumentException("'$name' doesn't have enough values")
							Log.w(
								TRACKPAD,
								"Expected between 2 and 5 arguments got $size instead. Ignoring superfluous args."
							)
							KeyText(data[0], data[1], data[2], data[3], data[4])
						}
					}
					keyboardLayout.put(id, keyText)
				}
		}
	}
	
	private fun updateKeyText(shift: Boolean = false, altGr: Boolean = false) {
		keyboardLayout.forEach { id, text ->
			val btn = findViewById<Button>(id)
			text?.also {
				btn.text = it.apply(shift, altGr)
				btn.visibility = VISIBLE
			}?:run{
				btn.visibility = INVISIBLE
			}
		}
	}
	
	fun onKeyTyped(view: View) {
		val btn = view as Button
		keyboardLayout[btn.id]?.also { keyText ->
			Network.event(this, ActionTextTyped(keyText.text))
		}?:run {
			Log.i(TRACKPAD, "No key defined in keyboardLayout for button ${btn.id}.")
		}
	}
	
	fun onSettingsClick(@Suppress("UNUSED_PARAMETER") view: View) {
		settingsResult.launch(Intent(this, PreferencesActivity::class.java))
	}
	
	fun onRightArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("RIGHT"))
	}
	
	fun onDownArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DOWN"))
	}
	
	fun onLeftArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("LEFT"))
	}
	
	fun onUpArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("UP"))
	}
	
	fun onPageUpClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("PAGE_UP"))
	}
	
	fun onPageDownClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("PAGE_DOWN"))
	}
	
	fun onPos1Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("POS1"))
	}
	
	fun onEndClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("END"))
	}
	
	fun onInsertClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("INSERT"))
	}
	
	fun onDelClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DEL"))
	}
	
	fun onEscClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("ESC"))
	}
	
	fun onF1Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F1"))
	}
	
	fun onF2Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F2"))
	}
	
	fun onF3Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F3"))
	}
	
	fun onF4Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F4"))
	}
	
	fun onF5Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F5"))
	}
	
	fun onF6Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F6"))
	}
	
	fun onF7Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F7"))
	}
	
	fun onF8Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F8"))
	}
	
	fun onF9Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F9"))
	}
	
	fun onF10Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F10"))
	}
	
	fun onF11Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F11"))
	}
	
	fun onF12Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F12"))
	}
	
	fun onBackSpaceClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("BACK_SPACE"))
	}
	
	fun onSpaceClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("SPACE"))
	}
	
	fun onEnterClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("ENTER"))
	}
	
	fun onCtrlClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("CTRL", btn.isChecked))
	}
	
	fun onShiftClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("SHIFT", btn.isChecked))
		val btnAltGr = findViewById<ToggleButton>(R.id.buttonAltGr)
		updateKeyText(btn.isChecked, btnAltGr.isChecked)
	}
	
	fun onAltClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("ALT", btn.isChecked))
	}
	
	fun onAltGrClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("ALTGR", btn.isChecked))
		val btnShift = findViewById<ToggleButton>(R.id.buttonShift2)
		updateKeyText(btnShift.isChecked, btn.isChecked)
	}
	
	fun onSuperClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("SUPER", btn.isChecked))
	}
	
	fun onTabClicked(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("TAB"))
	}
	
	fun onMouseClick(@Suppress("UNUSED_PARAMETER") view: View) {
		onBackPressedDispatcher.onBackPressed()
	}
}
