package kryptonbutterfly.hidoverwifi.dto

import com.google.gson.annotations.Expose
import java.io.DataOutputStream
import java.io.Serializable
import java.util.Arrays

private enum class Action {
	MOVE,
	BUTTON,
	CLICK,
	DOUBLE,
	SCROLL,
	KEY,
	TYPE,
	TEXT
}

sealed class InputAction: Serializable {
	override fun toString(): String {
		return "${this::class.simpleName}=(${
			Arrays.stream(this::class.java.declaredFields)
				.filter { f -> f.isAnnotationPresent(Expose::class.java) }
				.map { f ->
					val value = f.get(this)
					val name = f.name
					return@map if (CharSequence::class.java.isAssignableFrom(f.type))
						"$name='$value'"
					else
						"$name=$value"
				}
				.reduce { a, b -> "$a, $b" }
		})"
	}
	
	abstract fun write(oStream: DataOutputStream)
}

data class ActionMouseMove(val dx: Int, val dy: Int): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.MOVE.name)
		oStream.writeInt(dx)
		oStream.writeInt(dy)
	}
}

data class ActionMouseButton(val button: MouseButton, val down: Boolean): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.BUTTON.name)
		oStream.writeUTF(button.name)
		oStream.writeBoolean(down)
	}
}

data class ActionMouseClick(val button: MouseButton): InputAction(){
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.CLICK.name)
		oStream.writeUTF(button.name)
	}
}

data class ActionMouseDoubleClick(val button: MouseButton): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.DOUBLE.name)
		oStream.writeUTF(button.name)
	}
}

data class ActionMouseScroll(val x: Int, val y: Int): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.SCROLL.name)
		oStream.writeInt(x)
		oStream.writeInt(y)
	}
}

data class ActionKeyboardKey(val key: String, val down: Boolean): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.KEY.name)
		oStream.writeUTF(key)
		oStream.writeBoolean(down)
	}
}

data class ActionKeyboardType(val key: String): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.TYPE.name)
		oStream.writeUTF(key)
	}
}

data class ActionTextTyped(val text: String): InputAction() {
	override fun write(oStream: DataOutputStream) {
		oStream.writeUTF(Action.TEXT.name)
		oStream.writeUTF(text)
	}
}
