package kryptonbutterfly.hidoverwifi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kryptonbutterfly.hidoverwifi.Constants.TRACKPAD
import kryptonbutterfly.hidoverwifi.R
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardKey
import kryptonbutterfly.hidoverwifi.dto.ActionKeyboardType
import kryptonbutterfly.hidoverwifi.dto.ActionMouseButton
import kryptonbutterfly.hidoverwifi.dto.ActionMouseClick
import kryptonbutterfly.hidoverwifi.dto.ActionMouseDoubleClick
import kryptonbutterfly.hidoverwifi.dto.ActionMouseMove
import kryptonbutterfly.hidoverwifi.dto.ActionMouseScroll
import kryptonbutterfly.hidoverwifi.dto.InputAction
import kryptonbutterfly.hidoverwifi.dto.MouseButton
import kryptonbutterfly.hidoverwifi.network.Network
import kryptonbutterfly.hidoverwifi.prefs.prefs
import java.util.Arrays
import kotlin.jvm.optionals.getOrNull

class MousePadActivity : AppCompatActivity() {
	private var dragFlag = false
	
	private fun stopDrag() {
		dragFlag = false
	}
	
	private fun startDrag() {
		dragFlag = true
	}
	
	private val settingsResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			val scrollBar = findViewById<ConstraintLayout>(R.id.touch_pad_scroll_vertical)
			scrollBar.visibility = if (prefs(this).showScrollBar) VISIBLE else GONE
			
		}
	
	private val keyboardResult =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
	
	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_mouse_pad)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keyboardTopBar)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
		val vibrator = Arrays.stream(vibratorManager.vibratorIds)
			.mapToObj(vibratorManager::getVibrator)
			.filter(Vibrator::hasVibrator)
			.findFirst()
			.getOrNull()
		
		if (vibrator == null)
			Log.d(TRACKPAD, "No vibrator present! – Haptic feedback won't be available")
		
		val gestureDetector =
			GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
				
				override fun onScroll(
					e1: MotionEvent?,
					e2: MotionEvent,
					distanceX: Float,
					distanceY: Float
				): Boolean {
					when (e2.pointerCount) {
						1 -> {
							if (dragFlag)
								event(ActionMouseButton(MouseButton.LEFT, true))
							event(ActionMouseMove(distanceX.toInt(), distanceY.toInt()))
						}
						else -> {
							if (prefs(this@MousePadActivity).showScrollBar) {
								event(ActionMouseButton(MouseButton.LEFT, true))
								event(ActionMouseMove(distanceX.toInt(), distanceY.toInt()))
								startDrag()
							} else {
								event(ActionMouseScroll(distanceX.toInt(), distanceY.toInt()))
							}
						}
					}
					return super.onScroll(e1, e2, distanceX, distanceY)
				}
				
				override fun onDoubleTap(e: MotionEvent): Boolean {
					stopDrag()
					event(ActionMouseDoubleClick(MouseButton.LEFT))
					return super.onDoubleTap(e)
				}
				
				override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
					stopDrag()
					event(ActionMouseClick(MouseButton.LEFT))
					return super.onSingleTapConfirmed(e)
				}
				
				override fun onLongPress(e: MotionEvent) {
					stopDrag()
					event(ActionMouseClick(MouseButton.RIGHT))
					super.onLongPress(e)
				}
				
				override fun onFling(
					e1: MotionEvent?,
					e2: MotionEvent,
					velocityX: Float,
					velocityY: Float
				): Boolean {
					stopDrag()
					//TODO continue scrolling with momentum
					Log.d(TRACKPAD, "onFling($velocityX, $velocityY)")
					return super.onFling(e1, e2, velocityX, velocityY)
				}
				
				override fun onShowPress(e: MotionEvent) {
					stopDrag()
					if (!prefs(this@MousePadActivity).showScrollBar) {
						startDrag()
						vibrator?.vibrate(VibrationEffect.createOneShot(100, 0xFF))
					}
					super.onShowPress(e)
				}
			})
		
		val touchPad = findViewById<ConstraintLayout>(R.id.touch_pad)
		touchPad.setOnTouchListener(object : View.OnTouchListener {
			override fun onTouch(view: View?, event: MotionEvent?): Boolean {
				if (event == null)
					return false
				if (event.actionMasked == ACTION_UP)
					releasePointer()
				return gestureDetector.onTouchEvent(event)
			}
		})
		
		val scrollDetector = GestureDetector(this, object: GestureDetector.SimpleOnGestureListener() {
			override fun onScroll(
				e1: MotionEvent?,
				e2: MotionEvent,
				distanceX: Float,
				distanceY: Float
			): Boolean {
				val y = (distanceY / 2).toInt()
				if (y != 0)
					event(ActionMouseScroll(0, y))
				return true
			}
		})
		
		val scrollBar = findViewById<ConstraintLayout>(R.id.touch_pad_scroll_vertical)
		scrollBar.setOnTouchListener { view, event ->
			if (event == null)
				true
			scrollDetector.onTouchEvent(event)
		}
		scrollBar.visibility = if (prefs(this).showScrollBar) VISIBLE else GONE
	}
	
	override fun onPause() {
		super.onPause()
		prefs(this).save(this)
	}
	
	override fun onDestroy() {
		Network.disconnect(true)
		Log.v(TRACKPAD, "onDestroy")
		super.onDestroy()
	}
	
	private fun releasePointer() {
		if (!dragFlag)
			return
		stopDrag()
		event(ActionMouseButton(MouseButton.LEFT, false))
	}
	
	fun onSettingsClick(@Suppress("UNUSED_PARAMETER") view: View) {
		settingsResult.launch(Intent(this, PreferencesActivity::class.java))
	}
	
	fun onTabClicked(@Suppress("UNUSED_PARAMETER") view: View) {
		Network.event(this, ActionKeyboardType("TAB"))
	}
	
	fun onShiftClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("SHIFT", btn.isChecked))
	}
	
	fun onCtrlClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("CTRL", btn.isChecked))
	}
	
	fun onSuperClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("SUPER", btn.isChecked))
	}
	
	fun onAltClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("ALT", btn.isChecked))
	}
	
	fun onAltGrClick(@Suppress("UNUSED_PARAMETER") view: View) {
		val btn = view as ToggleButton
		Network.event(this, ActionKeyboardKey("ALTGR", btn.isChecked))
	}
	
	fun onKeyboardClick(@Suppress("UNUSED_PARAMETER") view: View) {
		keyboardResult.launch(Intent(this, KeyboardActivity::class.java))
	}
	
	private fun event(action: InputAction) {
		Network.event(this, action)
	}
}
