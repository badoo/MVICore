package com.badoo.mvicore.utils

import io.reactivex.exceptions.CompositeException
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Collections

class RxErrorRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                val handler = RxJavaPlugins.getErrorHandler()
                val errors = Collections.synchronizedCollection(ArrayList<Throwable>())
                RxJavaPlugins.setErrorHandler { errors.add(it) }
                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.setErrorHandler(handler)
                    if (errors.isNotEmpty()) {
                        throw CompositeException(errors)
                    }
                }
            }
        }
}
