package kryptonbutterfly.hidoverwifi.prefs

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kryptonbutterfly.hidoverwifi.Constants.KEYSTORE_ALIAS
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object SecureData {
	private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
	private lateinit var secretKey: SecretKey
	
	private fun init() {
		val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
		val purpose: Int = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		keyGenerator.init(KeyGenParameterSpec.Builder(
			KEYSTORE_ALIAS,
			purpose)
			.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
			.setUserAuthenticationRequired(true)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
			.build())
		secretKey = keyGenerator.generateKey()
	}
	
	fun load() {
		cipher.init(Cipher.DECRYPT_MODE, secretKey)
	}
	
	fun persist() {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey)
		val data = cipher.doFinal("Data".toByteArray())
		val iv = cipher.iv
		val output = ByteBuffer.allocate(4 + iv.size + data.size)
			.putInt(iv.size)
			.put(iv)
			.put(data)
			.array()
	}
}
