package com.badoo.mvicore2.lifecycle

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.badoo.mvicore2.lifecycle.Lifecycle.Event
import com.badoo.mvicore2.lifecycle.internal.Android
import com.badoo.mvicore2.lifecycle.internal.FromCompletableSource
import com.badoo.mvicore2.lifecycle.internal.FromObservableSource
import com.badoo.mvicore2.lifecycle.internal.ManualLifecycle
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer

interface Lifecycle : ObservableSource<Event> {

    fun startImmediately(): Lifecycle =
            observable(Observable.wrap(this)
                    .startWith(Event.START)
                    .distinctUntilChanged())

    enum class Event {
        START,
        STOP
    }

    interface Manual : Lifecycle, Observer<Event>

    companion object {

        fun manual(): Manual = ManualLifecycle()

        fun observable(source: ObservableSource<Event>) : Lifecycle = FromObservableSource(source)

        fun completable(source: CompletableSource) : Lifecycle = FromCompletableSource(source)

        fun activity(activity: AppCompatActivity) = Android(activity.lifecycle).startImmediately()

        fun fragment(fragment: Fragment) = Android(fragment.lifecycle).startImmediately()
    }
}

