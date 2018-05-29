package com.badoo.mvicore.extension

class SameThreadVerifier {

    companion object {
        var isEnabled : Boolean = true
    }

    private val originalThread by lazy { Thread.currentThread().id }

    fun verify() {
        if (isEnabled && (Thread.currentThread().id != originalThread)) {
            throw AssertionError("Not on main thread")
        }
    }

}
