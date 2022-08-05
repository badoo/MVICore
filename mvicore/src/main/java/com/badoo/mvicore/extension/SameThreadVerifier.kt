package com.badoo.mvicore.extension

class SameThreadVerifier(private val clazz: Class<*>) {

    companion object {
        var isEnabled : Boolean = true
    }

    private val originalThreadId: Long
    private val originalThreadName: String

    init {
        val currentThread = Thread.currentThread()
        originalThreadId = currentThread.id
        originalThreadName = currentThread.name
    }

    fun verify() {
        val currentThread = Thread.currentThread()
        if (isEnabled && (currentThread.id != originalThreadId)) {
            throw AssertionError(
                "${clazz.name} was interacted with on the wrong thread. " +
                        "Expected: '$originalThreadName', Actual: '${currentThread.name}'"
            )
        }
    }

}
