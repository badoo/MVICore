package com.badoo.mvicore.utils

import io.reactivex.rxjava3.exceptions.CompositeException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.util.Collections
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class RxErrorRule : BeforeEachCallback, AfterEachCallback {

    private val errors = Collections.synchronizedCollection(ArrayList<Throwable>())

    override fun beforeEach(context: ExtensionContext?) {
        errors.clear()
        RxJavaPlugins.setErrorHandler { errors += it }
    }

    override fun afterEach(context: ExtensionContext?) {
        RxJavaPlugins.reset()
        if (errors.isNotEmpty()) {
            fail<Unit>("RxJava errors found", CompositeException(errors))
        }
    }
}
