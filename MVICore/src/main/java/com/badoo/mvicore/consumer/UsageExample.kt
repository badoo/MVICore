package com.badoo.mvicore.consumer

import com.badoo.mvicore.consumer.middleware.LoggingMiddleWare
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware
import com.badoo.mvicore.consumer.middlewareconfig.MiddlewareConfiguration
import com.badoo.mvicore.consumer.middlewareconfig.Middlewares
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.AllOf
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.Always
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.AnyOf
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.Conditional
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.InstanceOf
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.Not
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.PackageName
import com.badoo.mvicore.consumer.util.Logger
import com.badoo.mvicore.feature.Feature

fun Unit.example() {
    val isInDebug = true
    val logger = object : Logger {
        override fun invoke(p1: String) {
            TODO("not implemented")
        }
    }

    /*
        Example middleware configuration:
        - add logging middleware: always
        - add playback middleware only if:
            - in debug
            - AND consumer in question is in any of the 3 packages
            - AND consumer is definitely not an instance of something
    */
    Middlewares.configurations.addAll(
        listOf(
            MiddlewareConfiguration(
                condition = Always,
                factories = listOf(
                    { consumer -> LoggingMiddleWare(consumer, logger) }
                )
            ),

            MiddlewareConfiguration(
                condition = AllOf(
                    Conditional { isInDebug },
                    AnyOf(
                        PackageName.SimpleMatcher("com.example.foo"),
                        PackageName.SimpleMatcher("com.example.bar"),
                        PackageName.SimpleMatcher("com.example.baz")
                    ),
                    Not(
                        InstanceOf(Feature::class.java) // some concrete class
                    )
                ),
                factories = listOf(
                    { consumer -> PlaybackMiddleware(consumer, TODO()) }
                )
            )
        )
    )
}
