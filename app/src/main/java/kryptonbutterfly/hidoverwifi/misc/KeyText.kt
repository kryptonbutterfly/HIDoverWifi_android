package kryptonbutterfly.hidoverwifi.misc

data class KeyText(
	val uniqueKeyName: String,
	val lower: String,
	val upper: String,
	val gr: String? = null,
	val upperGr: String? = null,
) {
	fun apply(shift: Boolean, altGr: Boolean): String {
		return if (shift)
			if (altGr)
				upperGr?:upper
			else
				upper
		else
			if (altGr)
				gr?:lower
			else
				lower
	}
}