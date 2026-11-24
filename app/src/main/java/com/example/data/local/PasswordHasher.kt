package com.example.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object PasswordHasher {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val salted = "$salt:$password".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(salted)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }
}
