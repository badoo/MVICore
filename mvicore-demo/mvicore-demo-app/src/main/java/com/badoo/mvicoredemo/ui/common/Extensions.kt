package com.badoo.mvicoredemo.ui.common

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.badoo.mvicoredemo.R
import com.badoo.mvicoredemo.ui.lifecycle.LifecycleDemoActivity
import com.badoo.mvicoredemo.ui.main.MainActivity
import com.google.android.material.navigation.NavigationView

fun NavigationView.init(drawerLayout: DrawerLayout, selectedIndex: Int) {
    setCheckedItem(selectedIndex)
    setNavigationItemSelectedListener { item ->
        item.isChecked = true
        drawerLayout.closeDrawers()
        val context = drawerLayout.context

        when (item.itemId) {
            R.id.drawer_lifecycle -> startActivity(context, LifecycleDemoActivity::class.java)
            R.id.drawer_main -> startActivity(context, MainActivity::class.java)
            else -> return@setNavigationItemSelectedListener false
        }

        return@setNavigationItemSelectedListener true
    }
}

fun startActivity(
    context: Context,
    activityClass: Class<out AppCompatActivity>
) {
    ContextCompat.startActivity(
        context,
        Intent(context, activityClass),
        null
    )
}
