package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {

    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashWithSalt(input: String, salt: String): String {
        val combined = input + salt
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(input: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashWithSalt(input, salt)
        return calculated.equals(expectedHash, ignoreCase = true)
    }
}
