package kryptonbutterfly.hidoverwifi.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kryptonbutterfly.hidoverwifi.Constants.KEYBOARD_LAYOUTS
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.prefs.prefs

class PreferencesActivity : AppCompatActivity() {
	private lateinit var layoutAdapter: ArrayAdapter<String>
	private lateinit var main: ConstraintLayout
	private lateinit var keyboardLayout: Spinner
	
	
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
		prefs.devices[prefs.currentDevice]?.also { device ->
			
			val switchScrollBar = findViewById<SwitchCompat>(R.id.switchScrollBar)
			keyboardLayout = findViewById(R.id.keyboardLayout)
			val copyFromHost = findViewById<SwitchCompat>(R.id.copyFromHost)
			
			switchScrollBar.isChecked = prefs.showScrollBar
			
			layoutAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
			layoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
			keyboardLayout.adapter = layoutAdapter
			
			copyFromHost.isChecked = prefs.copyFromHost
			
			onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					prefs.showScrollBar = switchScrollBar.isChecked
					
					prefs.keyboardLayout = keyboardLayout.selectedItem as String
					prefs.copyFromHost = copyFromHost.isChecked
					prefs.save(this@PreferencesActivity)
					finish()
				}
			})
		}
	}
	
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (!hasFocus)
			return
		
		val selectedLayout = (keyboardLayout.selectedItem as? String)
		layoutAdapter.clear()
		val availableLayouts = KEYBOARD_LAYOUTS.keys.toMutableList()
		layoutAdapter.addAll(availableLayouts)
		
		selectedLayout?.also { lName ->
			val index = availableLayouts.indexOfFirst { it == lName }
			if (index != -1)
				keyboardLayout.setSelection(index)
			else
				keyboardLayout.isSelected = false
		} ?: also {
			val prefsLayout = prefs(this).keyboardLayout
			if (prefsLayout.isNotEmpty()){
				val index = availableLayouts.indexOfFirst { it == prefsLayout }
				if (index != -1)
					keyboardLayout.setSelection(index)
				else
					keyboardLayout.isSelected = false
			} else
				keyboardLayout.isSelected = false
		}
	}
}