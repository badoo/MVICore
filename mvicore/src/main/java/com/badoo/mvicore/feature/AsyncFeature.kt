package com.badoo.mvicore.feature

import io.reactivex.rxjava3.core.Observable

/**
 * [Feature] that explicitly defines its multithreading capabilities.
 * Use [backgroundStates] and [backgroundNews] to receive updates on the reducer scheduler.
 * Default ones use the observation scheduler to provide backward compatibility.
 */
interface AsyncFeature<Wish : Any, State : Any, News : Any> : Feature<Wish, State, News> {

    val backgroundStates: Observable<State>

    val backgroundNews: Observable<News>

}
