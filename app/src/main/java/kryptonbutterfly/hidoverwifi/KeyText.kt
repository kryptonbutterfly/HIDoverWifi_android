package kryptonbutterfly.hidoverwifi

data class KeyText(
	val lower: String,
	val upper: String,
	val gr: String? = null,
	val upperGr: String? = null,
	val text: String = lower,
) {
	public fun apply(shift: Boolean, altGr: Boolean): String {
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
