package kryptonbutterfly.hidoverwifi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kryptonbutterfly.hidoverwifi.Constants.KEY_MAP
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardType
import kryptonbutterfly.hidoverwifi.network.Network

class KeyboardActivity : AppCompatActivity() {
	private val settingsResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_keyboard)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keyboardTopBar)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
	}
	
	private fun updateKeyText(shift: Boolean, altGr: Boolean) {
		KEY_MAP.forEach { id, text ->
			findViewById<Button>(id).text = text.apply(shift, altGr)
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
	
	fun onCircumflex(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("CIRCUMFLEX"))
	}
	
	fun onNum1(@Suppress("UNUSED_PARAMETER") view: View ) {
		Network.event(this, ActionKeyboardType("DIG1"))
	}
	
	fun onNum2(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG2"))
	}
	
	fun onNum3(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG3"))
	}
	
	fun onNum4(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG4"))
	}
	
	fun onNum5(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG5"))
	}
	
	fun onNum6(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG6"))
	}
	
	fun onNum7(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG7"))
	}
	
	fun onNum8(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG8"))
	}
	
	fun onNum9(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG9"))
	}
	
	fun onNum0(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DIG0"))
	}
	
	fun onMinus(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("MINUS"))
	}
	
	fun onSharpS(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("SHARP_S"))
	}
	
	fun onQ(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Q"))
	}
	
	fun onW(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("W"))
	}
	
	fun onE(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("E"))
	}
	
	fun onR(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("R"))
	}
	
	fun onT(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("T"))
	}
	
	fun onY(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Y"))
	}
	
	fun onU(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("U"))
	}
	
	fun onI(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("I"))
	}
	
	fun onO(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("O"))
	}
	
	fun onP(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("P"))
	}
	
	fun onA(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("A"))
	}
	
	fun onS(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("S"))
	}
	
	fun onD(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("D"))
	}
	
	fun onF(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("F"))
	}
	
	fun onG(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("G"))
	}
	
	fun onH(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("H"))
	}
	
	fun onJ(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("J"))
	}
	
	fun onK(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("K"))
	}
	
	fun onL(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("L"))
	}
	
	fun onZ(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Z"))
	}
	
	fun onX(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("X"))
	}
	
	fun onC(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("C"))
	}
	
	fun onV(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("V"))
	}
	
	fun onB(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("B"))
	}
	
	fun onN(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("N"))
	}
	
	fun onM(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("M"))
	}
	
	fun onAcuteAccent(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("ACUTE_ACCENT"))
	}
	
	fun onUE(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Ü"))
	}
	
	fun onPlus(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("PLUS"))
	}
	
	fun onOE(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Ö"))
	}
	
	fun onAE(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("Ä"))
	}
	
	fun onComma(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("COMMA"))
	}

	fun onDot(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("DOT"))
	}
	
	fun onHash(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("HASH"))
	}
	
	fun onLess(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("LESS"))
	}
	
	fun onMouseClick(@Suppress("UNUSED_PARAMETER") view: View) {
		onBackPressedDispatcher.onBackPressed()
	}
}
