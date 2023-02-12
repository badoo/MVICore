package com.badoo.mvicoredemo.auth

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.badoo.mvicoredemo.di.usersessionscope.UserManager
import com.badoo.mvicoredemo.ui.login.LoginActivity
import com.badoo.mvicoredemo.ui.main.MainActivity

fun AppCompatActivity.login() {
    storeIsLoggedIn(true)
    UserManager.getUserManager(application).userLoggedIn()

    ContextCompat.startActivity(
        this,
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        },
        Bundle()
    )
}

fun AppCompatActivity.logout() {
    storeIsLoggedIn(false)
    UserManager.getUserManager(application).logout()

    ContextCompat.startActivity(
        this,
        Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        },
        Bundle()
    )
}

fun AppCompatActivity.isLoggedIn(): Boolean =
    getSharedPreferences(PrefsHelper.PREF_KEY_LOGIN, MODE_PRIVATE)
        .getBoolean("auth", false)

fun AppCompatActivity.storeIsLoggedIn(isLoggedIn: Boolean) {
    getSharedPreferences(PrefsHelper.PREF_KEY_LOGIN, MODE_PRIVATE)
        .edit()
        .putBoolean("auth", isLoggedIn)
        .apply()
}

class PrefsHelper {
    companion object {
        const val PREF_KEY_LOGIN = "login"
    }
}
