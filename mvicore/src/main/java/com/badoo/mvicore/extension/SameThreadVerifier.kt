package com.badoo.mvicore.extension

class SameThreadVerifier(private val clazz: Class<*>) {

    companion object {
        var isEnabled : Boolean = true
    }

    private val originalThread = Thread.currentThread()

    fun verify() {
        val currentThread = Thread.currentThread()
        if (isEnabled && (currentThread.id != originalThread.id)) {
            throw AssertionError(
                "${clazz.name} was interacted with on the wrong thread. " +
                        "Expected: '${originalThread.name}', Actual: '${currentThread.name}'"
            )
        }
    }

}
