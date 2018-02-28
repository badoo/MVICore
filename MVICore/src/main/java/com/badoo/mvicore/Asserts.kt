package com.badoo.mvicore

import android.os.Looper
import android.support.annotation.VisibleForTesting

private val mainThread by lazy { Looper.getMainLooper().thread }
private var isEnabled = true

@VisibleForTesting
fun overrideAssertsForTesting(enabled: Boolean) {
    isEnabled = enabled
}

fun assertOnMainThread() {
    if (isEnabled && (Thread.currentThread() != mainThread)) {
        throw AssertionError("Not on main thread")
    }
}
