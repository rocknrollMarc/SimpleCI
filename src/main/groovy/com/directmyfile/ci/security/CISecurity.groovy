package com.directmyfile.ci.security

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.utils.Utils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CISecurity {
    private final CI ci

    CISecurity(CI ci) {
        this.ci = ci
    }

    boolean isSecure() {
        return ci.config.securitySection()["enabled"].asBoolean()
    }

    boolean register(String username, String password) {

        if (!secure) {
            return false
        }

        def matchedUsers = ci.sql.rows("SELECT `username`, `password` FROM `users` WHERE `username`=`${username}`")

        if (matchedUsers.size() != 0) {
            return false
        }
        def salt = Utils.generateSalt(1024)
        def key = new SecretKeySpec(salt, "HmacSHA512")
        def mac = Mac.getInstance("HmacSHA512")
        mac.init(key)
        def hashed = mac.doFinal(password.bytes)

        def hashHex = hashed.encodeHex().writeTo(new StringWriter()).toString()

        ci.sql.insert("INSERT INTO `users` (`username`, `pass`, `salt`) VALUES ('${username}', '${hashHex}', '${salt.encodeHex().writeTo(new StringWriter()).toString()}')")
        return true
    }

    boolean checkAccess(String username, String password) {

        if (!secure) {
            return true
        }

        def matchedUsers = ci.sql.rows("SELECT `username`, `password` FROM `users` WHERE `username`=`${username}`")

        if (matchedUsers.size() == 0) {
            return false
        }
        def user = matchedUsers[0]
        def key = new SecretKeySpec((user.getProperty("salt") as String).decodeHex(), "HmacSHA512")
        def mac = Mac.getInstance("HmacSHA512")
        mac.init(key)
        def hashed = mac.doFinal(password.bytes)
        return user.getProperty("pass").toString().decodeHex() == hashed
    }
}
