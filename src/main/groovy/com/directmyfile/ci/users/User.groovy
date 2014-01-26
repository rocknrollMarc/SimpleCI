package com.directmyfile.ci.users

import com.directmyfile.ci.core.CI

import java.security.MessageDigest
import java.security.SecureRandom

class User {

    CI ci
    String username

    User(CI ci, String username) {
        this.ci = ci
        this.username = username
    }

    String getUsername() {
        return username
    }

    String getPassword() {
    }

    String getSalt() {
    }

    static byte[] generateSalt(int size) {
        SecureRandom random = new SecureRandom()
        byte[] list = new byte[size]
        random.nextBytes(list)
        return list
    }

    static String generateHash(byte[] input) {
        def messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0')
    }

    static String toString(byte[] input) {
        return new BigInteger(1, input).toString(16).padLeft(40, '0')
    }

}
