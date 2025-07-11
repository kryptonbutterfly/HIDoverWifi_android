package kryptonbutterfly.hidoverwifi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Constants {
	val GSON: Gson = GsonBuilder()
		.setPrettyPrinting()
		.create()
	
	const val TRACKPAD = "TRACKPAD"
	
	val KEY_MAP: HashMap<Int, KeyText> = hashMapOf(
		R.id.keyboard_button_circumflex_accent to KeyText("^", "°", "′", "″"),
		R.id.keyboard_button_1 to KeyText("1", "!", "¹", "¡"),
		R.id.keyboard_button_2 to KeyText("2", "\"", "²", "⅛"),
		R.id.keyboard_button_3 to KeyText("3", "§", "³", "£"),
		R.id.keyboard_button_4 to KeyText("4", "$", "¼", "¤"),
		R.id.keyboard_button_5 to KeyText("5", "%", "½", "⅜"),
		R.id.keyboard_button_6 to KeyText("6", "&", "¬", "⅝"),
		R.id.keyboard_button_7 to KeyText("7", "/", "{", "⅞"),
		R.id.keyboard_button_8 to KeyText("8", "(", "[", "™"),
		R.id.keyboard_button_9 to KeyText("9", ")", "]", "±"),
		R.id.keyboard_button_0 to KeyText("0", "=", "}", "°"),
		R.id.keyboard_button_sharp_s to KeyText("ẞ", "?", "\\", "¿"),
		R.id.keyboard_button_acute_accent to KeyText("´", "`", "¸", "˛"),
		
		R.id.keyboard_button_q to KeyText("q", "Q", "@", "Ω"),
		R.id.keyboard_button_w to KeyText("w", "W", "ł", "Ł"),
		R.id.keyboard_button_e to KeyText("e", "E", "€", "€"),
		R.id.keyboard_button_r to KeyText("r", "R", "¶", "®"),
		R.id.keyboard_button_t to KeyText("t", "T", "ŧ", "Ŧ"),
		R.id.keyboard_button_z to KeyText("z", "Z", "←", "¥"),
		R.id.keyboard_button_u to KeyText("u", "U", "↓", "↑"),
		R.id.keyboard_button_i to KeyText("i", "I", "→", "ı"),
		R.id.keyboard_button_o to KeyText("o", "O", "ø", "Ø"),
		R.id.keyboard_button_p to KeyText("p", "P", "þ", "Þ"),
		R.id.keyboard_button_ü to KeyText("ü", "Ü", "¨", "°"),
		R.id.keyboard_button_plus to KeyText("+", "*", "~", "¯"),
		
		R.id.keyboard_button_a to KeyText("a", "A", "æ", "Æ"),
		R.id.keyboard_button_s to KeyText("s", "S", "ſ", "ẞ"),
		R.id.keyboard_button_d to KeyText("d", "D", "ð", "Ð"),
		R.id.keyboard_button_f to KeyText("f", "F", "đ", "ª"),
		R.id.keyboard_button_g to KeyText("g", "G", "ŋ", "Ŋ"),
		R.id.keyboard_button_h to KeyText("h", "H", "ħ", "Ħ"),
		R.id.keyboard_button_j to KeyText("j", "J", "̣", "˙"),
		R.id.keyboard_button_k to KeyText("k", "K", "ĸ", "&"),
		R.id.keyboard_button_l to KeyText("l", "L", "ł", "Ł"),
		R.id.keyboard_button_ö to KeyText("ö", "Ö", "˝", "̣"),
		R.id.keyboard_button_ä to KeyText("ä", "Ä", "^", "ˇ"),
		R.id.keyboard_button_hash to KeyText("#", "'", "’", "˘"),
		
		R.id.keyboard_button_less to KeyText("<", ">", "|", ""),
		R.id.keyboard_button_y to KeyText("y", "Y", "»", "›"),
		R.id.keyboard_button_x to KeyText("x", "X", "«", "‹"),
		R.id.keyboard_button_c to KeyText("c", "C", "¢", "©"),
		R.id.keyboard_button_v to KeyText("v", "V", "„", "‚"),
		R.id.keyboard_button_b to KeyText("b", "B", "“", "‘"),
		R.id.keyboard_button_n to KeyText("n", "N", "”", "’"),
		R.id.keyboard_button_m to KeyText("m", "M", "µ", "º"),
		R.id.keyboard_button_comma to KeyText(",", ";", "·", "×"),
		R.id.keyboard_button_dot to KeyText(".", ":", "…", "÷"),
		R.id.keyboard_button_minus to KeyText("-", "_", "–", "—")
	)
}
