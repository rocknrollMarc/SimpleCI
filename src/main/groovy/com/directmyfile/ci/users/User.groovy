package com.directmyfile.ci.users

import com.directmyfile.ci.core.CI

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

}
