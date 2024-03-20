package com.mistershorr.loginandregistration

import java.util.regex.Pattern

// object keyword makes it so all the functions are
// static functions
object RegistrationUtil {
    // use this in the test class for the is username taken test
    // make another similar list for some taken emails
    var existingUsers = listOf("cosmicF", "cosmicY", "bob", "alice")
    var exitingEmails = listOf("admin@admin.net", "e@e.com", "user@gmail.com", "random@company.org")
//    you can use listOf<type>() instead of making the list & adding individually
//    List<String> blah = new ArrayList<String>();
//    blah.add("hi")
//    blah.add("hello")

    // isn't empty
    // isn't already taken
    // minimum number of characters is 3
    fun validateUsername(username: String) : Boolean {
        if(username.isEmpty()) {
            return false
        }
        if(existingUsers.contains(username)) {
            return false
        }
        if(username.length < 3) {
            return false
        }
        return true
    }

    // make sure meets security requirements (deprecated ones that are still used everywhere)
    // min length 8 chars
    // at least one digit
    // at least one capital letter
    // both passwords match
    // not empty
    fun validatePassword(password: String, confirmPassword: String) : Boolean {
        if(password.length < 8 || confirmPassword.length < 8) {
            return false
        }

        password.count { it.isDigit() } > 0
        password.count { it.isUpperCase() } > 0

        if(password != confirmPassword) {
            return false
        }

        if(password.isEmpty() || confirmPassword.isEmpty()) {
            return false
        }

        return true
    }

    // isn't empty
    fun validateName(name: String) : Boolean {
        return name.isNotEmpty()
    }

    // isn't empty
    // make sure the email isn't used
    // make sure it's in the proper email format user@domain.tld
    fun validateEmail(email: String) : Boolean {
        if(email.isEmpty()) {
            return false
        }
        if(exitingEmails.contains(email)) {
            return false
        }
        if(!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            return false
        }
        return true
    }
}