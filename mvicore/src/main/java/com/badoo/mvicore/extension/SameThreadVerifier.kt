package com.badoo.mvicore.extension

class SameThreadVerifier {

    companion object {
        var isEnabled : Boolean = true
        // TODO: Performance, temp to skip throw while debugging issues
        var debugDontThrow : Boolean = true
    }

    private val originalThread = Thread.currentThread().id

    fun verify() {
        if (isEnabled && (Thread.currentThread().id != originalThread)) {
            if (!debugDontThrow) {
                throw AssertionError("Not on same thread as previous verification")
            }
        }
    }

}
