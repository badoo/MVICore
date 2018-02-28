package com.badoo.mvicore.factory

import com.badoo.mvicore.core.Configuration
import com.badoo.mvicore.core.DefaultFeature
import com.badoo.mvicore.core.Feature

/**
 * Will create a DefaultEngine with the supplied configuration
 */
class DefaultFeatureFactory : FeatureFactory {

    override fun <State : Any, Wish : Any, Effect : Any> create(configuration: Configuration<State, Wish, Effect>): Feature<State, Wish> =
            DefaultFeature(configuration)
}
