package com.badoo.mvicoredemo.auth

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionScopedComponent
import com.badoo.mvicoredemo.ui.login.LoginActivity
import com.badoo.mvicoredemo.ui.main.MainActivity


fun AppCompatActivity.login() {
    storeIsLoggedIn(true)
    UserSessionScopedComponent.get()
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
    UserSessionScopedComponent.destroy()
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
