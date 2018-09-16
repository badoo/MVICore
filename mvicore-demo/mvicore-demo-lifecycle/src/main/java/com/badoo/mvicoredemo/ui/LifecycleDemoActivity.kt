package com.badoo.mvicoredemo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import com.badoo.mvicore.android.lifecycle.ResumePauseBinderLifecycle
import com.badoo.mvicore.android.lifecycle.StartStopBinderLifecycle
import com.badoo.mvicore.binder.Binder
import com.badoo.mvicoredemo.R
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class LifecycleDemoActivity : AppCompatActivity() {

    private val events = PublishSubject.create<String>()
    private val createDestroyConsumer = LoggingConsumer("Lifecycle#CreateDestroy")
    private val startStopConsumer     = LoggingConsumer("Lifecycle#StartStop")
    private val resumePauseConsumer   = LoggingConsumer("Lifecycle#ResumePause")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lifecycle_demo_activity)

        Binder(CreateDestroyBinderLifecycle(lifecycle))
            .bind(events to createDestroyConsumer)

        Binder(StartStopBinderLifecycle(lifecycle))
            .bind(events to startStopConsumer)

        Binder(ResumePauseBinderLifecycle(lifecycle))
            .bind(events to resumePauseConsumer)

        events.onNext("onCreate")
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
}

private class LoggingConsumer(private val tag: String): Consumer<String> {
    override fun accept(message: String) {
        Log.d(tag, message)
    }

}
