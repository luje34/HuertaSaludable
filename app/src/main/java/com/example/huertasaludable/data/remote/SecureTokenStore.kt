package com.example.huertasaludable.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface AccessTokenStore {
    fun save(token: String)
    fun read(): String?
    fun clear()
}

class SecureTokenStore(context: Context) : AccessTokenStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    @Synchronized
    override fun save(token: String) {
        require(token.isNotBlank()) { "El token de acceso está vacío" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val storedValue = cipher.iv + encrypted
        check(
            preferences.edit()
                .putString(TOKEN_KEY, Base64.encodeToString(storedValue, Base64.NO_WRAP))
                .commit()
        ) { "No se pudo guardar la sesión segura" }
    }

    @Synchronized
    override fun read(): String? {
        val encoded = preferences.getString(TOKEN_KEY, null) ?: return null
        val storedValue = Base64.decode(encoded, Base64.NO_WRAP)
        require(storedValue.size > IV_LENGTH) { "El token almacenado está dañado" }
        val iv = storedValue.copyOfRange(0, IV_LENGTH)
        val encrypted = storedValue.copyOfRange(IV_LENGTH, storedValue.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    @Synchronized
    override fun clear() {
        check(preferences.edit().remove(TOKEN_KEY).commit()) {
            "No se pudo borrar la sesión segura"
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "secure_session"
        const val TOKEN_KEY = "access_token"
        const val KEY_ALIAS = "huerta_api_session_key"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
