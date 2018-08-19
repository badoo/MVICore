package com.badoo.mvicoredemo.ui.launcher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.badoo.mvicoredemo.auth.isLoggedIn
import com.badoo.mvicoredemo.auth.login
import com.badoo.mvicoredemo.auth.logout

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isLoggedIn()) login() else logout()
    }
}
