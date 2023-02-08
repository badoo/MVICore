package com.badoo.mvicore.extension

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class SameThreadVerifierTest {

    @Test
    fun `GIVEN same thread WHEN verify THEN expect no exceptions`() {
        val threadVerifier = SameThreadVerifier(String::class.java)
        threadVerifier.verify()
    }

    @Test
    fun `GIVEN different thread WHEN verify THEN expect exception`() {
        val threadVerifier = SameThreadVerifier(String::class.java)
        val testWorkerThreadName = Thread.currentThread().name

        var assertionError: AssertionError? = null
        val latch = CountDownLatch(1)
        thread(name = "wrong-thread") {
            try {
                threadVerifier.verify()
            } catch (e: AssertionError) {
                assertionError = e
            }
            latch.countDown()
        }
        latch.await(1, TimeUnit.SECONDS)

        assertNotNull(assertionError)
        assertEquals(
            "java.lang.String was interacted with on the wrong thread. Expected: '$testWorkerThreadName', Actual: 'wrong-thread'",
            assertionError!!.message
        )
    }
}
