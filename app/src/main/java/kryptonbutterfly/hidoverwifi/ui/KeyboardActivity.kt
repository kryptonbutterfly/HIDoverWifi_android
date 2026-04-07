package kryptonbutterfly.hidoverwifi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
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
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_ALT
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_CTRL
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_META
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_LEFT_SHIFT
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_RIGHT_ALT
import kryptonbutterfly.hidoverwifi.Constants.Keys.KEY_TAB
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.misc.KeyText
import kryptonbutterfly.hidoverwifi.network.Network
import kryptonbutterfly.hidoverwifi.prefs.prefs

class KeyboardActivity : AppCompatActivity() {
	private lateinit var shiftButton: ToggleButton
	private lateinit var ctrlButton: ToggleButton
	private lateinit var metaButton: ToggleButton
	private lateinit var altButton: ToggleButton
	private lateinit var altGrButton: ToggleButton
	private val settingsResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			loadKeyboardLayout()
			updateKeyText()
		}
	
	private val keyboardLayout: HashMap<Int, KeyText?> = HashMap()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_keyboard)
		
		Network.prepareConnection(this)
		
		shiftButton = findViewById(R.id.buttonShift2)
		ctrlButton = findViewById(R.id.buttonCtrl2)
		metaButton = findViewById(R.id.buttonSuper)
		altButton = findViewById(R.id.buttonAlt)
		altGrButton = findViewById(R.id.buttonAltGr)
		
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keyboardTopBar)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		loadKeyboardLayout()
		attachKeyListener()
		updateKeyText()
	}
	
	override fun onResume() {
		Network.pressedKeys {
			shiftButton.isChecked = it.shift
			ctrlButton.isChecked = it.ctrl
			metaButton.isChecked = it.meta
			altButton.isChecked = it.alt
			altGrButton.isChecked = it.altGr
		}
		super.onResume()
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
				btn.setOnTouchListener(KeyListener(it.uniqueKeyName))
				btn.visibility = VISIBLE
			}?:run{
				btn.setOnTouchListener(null)
				btn.visibility = INVISIBLE
			}
		}
	}
	
	private fun attachKeyListener() {
		fun attach(id: Int, key: String) {
			findViewById<View>(id).setOnTouchListener(KeyListener(key))
		}
		attach(R.id.buttonRightArrow, "KEY_RIGHT")
		attach(R.id.buttonDownArrow, "KEY_DOWN")
		attach(R.id.buttonLeftArrow, "KEY_LEFT")
		attach(R.id.buttonUpArrow, "KEY_UP")
		
		attach(R.id.buttonPictureUp, "KEY_PAGE_UP")
		attach(R.id.buttonPictureDown, "KEY_PAGE_DOWN")
		
		attach(R.id.buttonPos1, "KEY_HOME")
		attach(R.id.buttonEnd, "KEY_END")
		
		attach(R.id.buttonInsert, "KEY_INSERT")
		attach(R.id.buttonDel, "KEY_DELETE")
		
		attach(R.id.keyboard_button_Esc, "KEY_ESC")
		attach(R.id.keyboard_button_F1, "KEY_F1")
		attach(R.id.keyboard_button_F2, "KEY_F2")
		attach(R.id.keyboard_button_F3, "KEY_F3")
		attach(R.id.keyboard_button_F4, "KEY_F4")
		attach(R.id.keyboard_button_F5, "KEY_F5")
		attach(R.id.keyboard_button_F6, "KEY_F6")
		attach(R.id.keyboard_button_F7, "KEY_F7")
		attach(R.id.keyboard_button_F8, "KEY_F8")
		attach(R.id.keyboard_button_F9, "KEY_F9")
		attach(R.id.keyboard_button_F10, "KEY_F10")
		attach(R.id.keyboard_button_F11, "KEY_F11")
		attach(R.id.keyboard_button_F12, "KEY_F12")
		
		attach(R.id.keyboard_button_backspace, "KEY_BACKSPACE")
		attach(R.id.keyboard_button_space, "KEY_SPACE")
		attach(R.id.keyboard_button_enter, "KEY_ENTER")
		attach(R.id.buttonTab, KEY_TAB)
		
	}
	
	
	fun onSettingsClick(@Suppress("UNUSED_PARAMETER") view: View) {
		settingsResult.launch(Intent(this, PreferencesActivity::class.java))
	}
	
	fun onCtrlClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey(KEY_LEFT_CTRL, btn.isChecked))
	}
	
	fun onShiftClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey(KEY_LEFT_SHIFT, btn.isChecked))
		val btnAltGr = findViewById<ToggleButton>(R.id.buttonAltGr)
		updateKeyText(btn.isChecked, btnAltGr.isChecked)
	}
	
	fun onAltClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey(KEY_LEFT_ALT, btn.isChecked))
	}
	
	fun onAltGrClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey(KEY_RIGHT_ALT, btn.isChecked))
		val btnShift = findViewById<ToggleButton>(R.id.buttonShift2)
		updateKeyText(btnShift.isChecked, btn.isChecked)
	}
	
	fun onSuperClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey(KEY_LEFT_META, btn.isChecked))
	}
	
	fun onMouseClick(@Suppress("UNUSED_PARAMETER") view: View) {
		onBackPressedDispatcher.onBackPressed()
	}
	
	class KeyListener(val keyID: String) : View.OnTouchListener {
		override fun onTouch(view: View, event: MotionEvent): Boolean {
			when (event.actionMasked) {
				MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
					view.isPressed = true
					Network.event(view.context, ActionKeyboardKey(this.keyID, true))
				}
				MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
					view.isPressed = false
					Network.event(view.context, ActionKeyboardKey(this.keyID, false))
					view.performClick()
				}
			}
			return true
		}
	}
	
}
