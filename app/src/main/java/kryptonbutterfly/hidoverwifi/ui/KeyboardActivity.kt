package kryptonbutterfly.hidoverwifi.ui

import android.annotation.SuppressLint
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
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.KeyText
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardType
import kryptonbutterfly.hidoverwifi.network.Network
import kryptonbutterfly.hidoverwifi.prefs.prefs

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
		
		Network.ensureConnection(this)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keyboardTopBar)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		loadKeyboardLayout()
		updateKeyText()
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
					@SuppressLint("DiscouragedApi")
					val id = resources.getIdentifier(name, "id", packageName)
					val keyText: KeyText? = when (val size = data.size) {
						0 -> null
						3 -> KeyText(data[0], data[1], data[2])
						4 -> KeyText(data[0], data[1], data[2], data[3])
						5 -> KeyText(data[0], data[1], data[2], data[3], data[4])
						else -> {
							if (size < 2)
								throw IllegalArgumentException("'$name' doesn't have enough values")
							Log.w(
								TRACKPAD,
								"Expected between 3 and 5 arguments for '$name', got $size instead. Ignoring superfluous args."
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
			Network.event(this, ActionKeyboardType(keyText.uniqueKeyName))
		}?:run {
			Log.i(TRACKPAD, "No key defined in keyboardLayout for button ${btn.id}.")
		}
	}
	
	fun onSettingsClick(@Suppress("UNUSED_PARAMETER") view: View) {
		settingsResult.launch(Intent(this, PreferencesActivity::class.java))
	}
	
	fun onRightArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_RIGHT"))
	}
	
	fun onDownArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_DOWN"))
	}
	
	fun onLeftArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_LEFT"))
	}
	
	fun onUpArrowClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_UP"))
	}
	
	fun onPageUpClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_PAGE_UP"))
	}
	
	fun onPageDownClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_PAGE_DOWN"))
	}
	
	fun onPos1Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_HOME"))
	}
	
	fun onEndClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_END"))
	}
	
	fun onInsertClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_INSERT"))
	}
	
	fun onDelClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_DELETE"))
	}
	
	fun onEscClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_ESC"))
	}
	
	fun onF1Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F1"))
	}
	
	fun onF2Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F2"))
	}
	
	fun onF3Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F3"))
	}
	
	fun onF4Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F4"))
	}
	
	fun onF5Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F5"))
	}
	
	fun onF6Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F6"))
	}
	
	fun onF7Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F7"))
	}
	
	fun onF8Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F8"))
	}
	
	fun onF9Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F9"))
	}
	
	fun onF10Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F10"))
	}
	
	fun onF11Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F11"))
	}
	
	fun onF12Click(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_F12"))
	}
	
	fun onBackSpaceClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_BACKSPACE"))
	}
	
	fun onSpaceClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_SPACE"))
	}
	
	fun onEnterClick(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_ENTER"))
	}
	
	fun onCtrlClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("KEY_LEFT_CTRL", btn.isChecked))
	}
	
	fun onShiftClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("KEY_LEFT_SHIFT", btn.isChecked))
		val btnAltGr = findViewById<ToggleButton>(R.id.buttonAltGr)
		updateKeyText(btn.isChecked, btnAltGr.isChecked)
	}
	
	fun onAltClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("KEY_LEFT_ALT", btn.isChecked))
	}
	
	fun onAltGrClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("KEY_RIGHT_ALT", btn.isChecked))
		val btnShift = findViewById<ToggleButton>(R.id.buttonShift2)
		updateKeyText(btnShift.isChecked, btn.isChecked)
	}
	
	fun onSuperClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("KEY_LEFT_META", btn.isChecked))
	}
	
	fun onTabClicked(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("KEY_TAB"))
	}
	
	fun onMouseClick(@Suppress("UNUSED_PARAMETER") view: View) {
		onBackPressedDispatcher.onBackPressed()
	}
}
