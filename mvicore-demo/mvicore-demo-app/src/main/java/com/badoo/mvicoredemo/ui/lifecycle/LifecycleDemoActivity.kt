package com.badoo.mvicoredemo.ui.lifecycle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import com.badoo.mvicore.android.lifecycle.ResumePauseBinderLifecycle
import com.badoo.mvicore.android.lifecycle.StartStopBinderLifecycle
import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.named
import com.badoo.mvicoredemo.R
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*

class LifecycleDemoActivity : AppCompatActivity() {

    private val events = PublishSubject.create<String>()
    private val dummyConsumer = Consumer<String> {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle_demo)

        Binder(CreateDestroyBinderLifecycle(lifecycle))
            .bind(events to dummyConsumer named "Lifecycle#CreateDestroy")

        Binder(StartStopBinderLifecycle(lifecycle))
            .bind(events to dummyConsumer named "Lifecycle#StartStop")

        Binder(ResumePauseBinderLifecycle(lifecycle))
            .bind(events to dummyConsumer named "Lifecycle#ResumePause")

        events.onNext("onCreate")
        setupDrawer()
    }

    private fun setupDrawer() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.apply {
            setCheckedItem(0)
            setNavigationItemSelectedListener { item ->
                item.isChecked = true
                drawerLayout.closeDrawers()

                when (item.itemId) {
                    R.id.drawer_main -> finish()
                }

                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()

        events.onNext("onStart")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        events.onNext("onRestoreState")
    }

    override fun onResume() {
        super.onResume()

        events.onNext("onResume")
    }

    override fun onPostResume() {
        super.onPostResume()

        events.onNext("onPostResume")
    }

    override fun onPause() {
        super.onPause()

        events.onNext("onPause")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        events.onNext("onSaveState")
    }

    override fun onStop() {
        super.onStop()

        events.onNext("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()

        events.onNext("onDestroy")
    }

    companion object {
        fun start(context: Context) {
            ContextCompat.startActivity(
                context,
                Intent(context, LifecycleDemoActivity::class.java),
                null
            )
        }
    }
}
