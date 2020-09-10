package com.badoo.mvicoredemo.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.badoo.mvicoredemo.R
import com.badoo.mvicoredemo.auth.login
import kotlinx.android.synthetic.main.activity_login.signIn

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signIn.setOnClickListener { login() }
    }
}
