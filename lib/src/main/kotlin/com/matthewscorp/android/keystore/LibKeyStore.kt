package com.matthewscorp.android.keystore

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Calendar;
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal

/**
 * One possible implementation of an api 19+ compatible secure storage
 */
object LibKeyStore {
    private val ANDROID_KEY_STORE = "AndroidKeyStore"

    // Note: transfromation strings below
    // are in the format "algorithm/mode/padding"

    // Symmetric cipher >= api 23
    private val TRANSFORMATION_23 = "AES/CBC/PKCS7Padding"
    // Asymmetric cipher < api 23
    private val ALGORITHM_19 = "RSA"
    private val TRANSFORMATION_19 = ALGORITHM_19 + "/ECB/PKCS1Padding"
    val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)

    init {
        keyStore.load(null)
    }

    /**
     * Creates a key in the android keystore that will be referenced by the provided alias
     */
    fun createKey(context: Context, keyAlias: String) {
        if (!keyStore.containsAlias(keyAlias)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                keyStore.load(null)
                keyGenerator.init(KeyGenParameterSpec.Builder(keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build())
                keyGenerator.generateKey()
            } else {
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 1)
                val spec = KeyPairGeneratorSpec.Builder(context)
                        .setAlias(keyAlias)
                        .setSubject(X500Principal("CN=matthewscorp.com"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
                val generator = KeyPairGenerator.getInstance(ALGORITHM_19, ANDROID_KEY_STORE)
                generator.initialize(spec)
                generator.generateKeyPair()
            }
        }
    }

    fun deleteKey(keyAlias: String) {
        keyStore.deleteEntry(keyAlias)
    }

    /**
     * @return a kotlin.Pair of a Base64 encoded string of ciphertext result of the provided plaintext and an optional initialization vector array
     */
    fun encryptString(keyAlias: String, plainText: String): Pair<String, ByteArray?> {
        val outputStream = ByteArrayOutputStream()
        val iv: ByteArray?
        val cipher: Cipher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cipher = Cipher.getInstance(TRANSFORMATION_23)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(keyAlias, null))
            iv = cipher.iv
        } else {
            cipher = Cipher.getInstance(TRANSFORMATION_19)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getCertificate(keyAlias).publicKey)
            iv = null
        }
        val cipherOutputStream = CipherOutputStream(
                outputStream, cipher)
        cipherOutputStream.write(plainText.toByteArray(StandardCharsets.UTF_8))
        cipherOutputStream.close()
        return Pair(Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT), iv)
    }


    /**
     * @return Decrypted version of provided base64 encoded ciphertext ad initialization vector
     */
    fun decryptString(keyAlias: String, base64CipherText: String, iv: ByteArray?): String {
        val cipher: Cipher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val privateKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
            cipher = Cipher.getInstance(TRANSFORMATION_23)
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.secretKey, IvParameterSpec(iv))
        } else {
            val privateKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
            cipher = Cipher.getInstance(TRANSFORMATION_19)
            cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
        }
        val cipherInputStream = CipherInputStream(ByteArrayInputStream(Base64.decode(base64CipherText, Base64.DEFAULT)), cipher)
        val values = ArrayList<Byte>()
        var nextByte: Int = 0
        while (cipherInputStream.read().let { nextByte = it; it != -1 }) {
            values.add(nextByte.toByte())
        }
        val bytes = ByteArray(values.size)
        for (i in bytes.indices) {
            bytes[i] = values[i]
        }
        return String(bytes, 0, bytes.size, StandardCharsets.UTF_8)
    }
}