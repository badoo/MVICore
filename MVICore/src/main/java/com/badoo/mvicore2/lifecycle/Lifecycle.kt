package com.badoo.mvicore2.lifecycle

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.badoo.mvicore2.lifecycle.internal.Android
import com.badoo.mvicore2.lifecycle.internal.FromObservableSource
import com.badoo.mvicore2.lifecycle.internal.ManualLifecycle
import io.reactivex.ObservableSource
import io.reactivex.Observer

interface Lifecycle : ObservableSource<Lifecycle.Event> {

    enum class Event {
        START,
        STOP
    }

    interface Manual : Lifecycle, Observer<Event>

    companion object {

        fun manual(): Manual = ManualLifecycle()

        fun custom(source: ObservableSource<Event>) : Lifecycle = FromObservableSource(source)

        fun activity(activity: AppCompatActivity) = Android(activity.lifecycle)

        fun fragment(fragment: Fragment) = Android(fragment.lifecycle)
    }

}
