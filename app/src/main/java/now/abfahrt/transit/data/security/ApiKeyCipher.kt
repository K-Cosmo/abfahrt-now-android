package now.abfahrt.transit.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts locally persisted API keys with a non-exportable Android Keystore key.
 *
 * The ciphertext format is versioned so storage can evolve without guessing:
 *   enc:v1:<base64 iv>:<base64 ciphertext+tag>
 */
@Singleton
class ApiKeyCipher @Inject constructor() {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "abfahrt_api_keys_aes_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFIX = "enc:v1:"
        private const val GCM_TAG_BITS = 128
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    fun encrypt(plainText: String): String {
        require(plainText.isNotEmpty()) { "Refusing to encrypt an empty API key" }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        val encoder = Base64.getEncoder().withoutPadding()
        return buildString {
            append(PREFIX)
            append(encoder.encodeToString(cipher.iv))
            append(':')
            append(encoder.encodeToString(ciphertext))
        }
    }

    fun decrypt(storedValue: String): String {
        require(isEncrypted(storedValue)) { "Value is not in encrypted API-key format" }

        val payload = storedValue.removePrefix(PREFIX)
        val separator = payload.indexOf(':')
        require(separator > 0 && separator < payload.lastIndex) { "Invalid encrypted API-key payload" }

        val decoder = Base64.getDecoder()
        val iv = decoder.decode(payload.substring(0, separator))
        val ciphertext = decoder.decode(payload.substring(separator + 1))
        val key = getExistingKey() ?: error("Android Keystore API-key key is missing")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        getExistingKey()?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun getExistingKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    }
}
