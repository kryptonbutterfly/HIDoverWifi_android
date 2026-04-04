package kryptonbutterfly.hidoverwifi.network

data class PressedKeys(
	var shift: Boolean = false,
	var ctrl: Boolean = false,
	var meta: Boolean = false,
	var alt: Boolean = false,
	var altGr: Boolean = false)
