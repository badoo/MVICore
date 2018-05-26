package com.badoo.mvicore.extension

import android.os.Looper

private val mainThread by lazy { Looper.getMainLooper().thread }
private var isEnabled = true

fun overrideAssertsForTesting(enabled: Boolean) {
    isEnabled = enabled
}

fun assertOnMainThread() {
    if (isEnabled && (Thread.currentThread() != mainThread)) {
        throw AssertionError("Not on main thread")
    }
}
