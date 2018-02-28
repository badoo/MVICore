package com.badoo.mvicore.factory

import com.badoo.mvicore.core.Configuration
import com.badoo.mvicore.core.Feature

/**
 * Will create an implementation of Feature using a Configuration
 */
interface FeatureFactory {

    fun <State : Any, Wish : Any, Effect : Any> create(configuration: Configuration<State, Wish, Effect>): Feature<State, Wish>
}
