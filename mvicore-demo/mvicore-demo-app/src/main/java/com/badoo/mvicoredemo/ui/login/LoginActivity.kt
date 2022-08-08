package com.badoo.mvicoredemo.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.badoo.mvicoredemo.R
import com.badoo.mvicoredemo.auth.login

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<View>(R.id.signIn).setOnClickListener { login() }
    }
}
