package kryptonbutterfly.hidoverwifi

data class KeyText(
	val lower: String,
	val upper: String,
	val gr: String? = null,
	val upperGr: String? = null
) {
	public fun apply(shift: Boolean, altGr: Boolean): String {
		if (shift) {
			if (altGr && upperGr != null)
				return upperGr
			return upper
		} else {
			if (altGr && gr != null)
				return gr
			return lower
		}
	}
}
