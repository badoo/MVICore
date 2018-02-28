package com.badoo.mvicore.core

import android.support.annotation.MainThread
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

/**
 * Abstract implementation of [Boundary]. Transforms UI events into Wishes
 * using provided event filters and redirects them into appropriate Features.
 *
 * @param uiEvents       Observable of UI events. Should emit on the main thread.
 * @param featureHolders array of [FeatureHolder]
 * @param Event          type of UI events
 */
abstract class AbstractBoundary<Event : Any>(
        uiEvents: Observable<Event>,
        private val featureHolders: List<FeatureHolder<Event, *>>
) : Boundary {

    private val disposables = CompositeDisposable()

    init {
        featureHolders.forEach { connectFeature(it, uiEvents) }
    }

    private fun <Event : Any, Wish : Any> connectFeature(
        featureHolder: FeatureHolder<Event, Wish>,
        uiEvents: Observable<Event>
    ) = with(featureHolder) {
        disposables += uiEvents
            .mapNotNull { eventFilter.filterUiEvent(it) }
            .subscribe { feature.onWish(it) }
    }

    private inline fun <T, Wish> Observable<T>.mapNotNull(crossinline func: (T) -> Wish?): Observable<Wish> =
            flatMapMaybe { func.invoke(it)?.let { Maybe.just(it) } ?: Maybe.empty() }

    /**
     * Disposes the boundary and all its subscriptions along with all [Feature] instances
     * Should be called at the end of lifecycle.
     * Also disposes all the necessary Features.
     */
    override fun dispose() {
        disposables.dispose()
        featureHolders.forEach {
            if (it.isDisposable) {
                it.feature.dispose()
            }
        }
    }

    /**
     * Returns whether this boundary is disposed or not
     */
    override fun isDisposed(): Boolean = disposables.isDisposed

    /**
     * Event Filter that converts UI Events and News into Wishes
     *
     * @param Event type of UI Events
     * @param Wish  type of Feature's Wishes
     */
    interface EventFilter<in Event : Any, out Wish : Any> {

        /**
         * Converts UI Event into Wish, called on Main thread.
         * Method can return null if there is no corresponding Wish.
         *
         * @param event UI event
         * @return      Wish or null if there is no corresponding Wish
         */
        @MainThread
        fun filterUiEvent(event: Event): Wish? = null
    }

    /**
     * Holder class containing Feature and its Event Filter
     *
     * @param feature      see [Feature]
     * @param eventFilter  see [EventFilter]
     * @param isDisposable whether this Feature should be disposed at the end of life cycle or not, default is true
     * @param Event        type of UI Events
     * @param Wish         type of Feature's Wishes
     */
    class FeatureHolder<in Event : Any, Wish : Any>(
            val feature: Feature<*, Wish>,
            val eventFilter: EventFilter<Event, Wish>,
            val isDisposable: Boolean = true
    )
}
