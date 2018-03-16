package com.badoo.mvicore.boundary

import com.badoo.mvicore.core.Store
import io.reactivex.Observable
import io.reactivex.Observable.empty
import io.reactivex.Observable.just
import io.reactivex.subjects.PublishSubject

/**
 * TODO
 */
abstract class MultiFeatureBoundary<in UiEvent : Any, Wish : Any, ViewModel : Any>(
    private val featureHolders: List<FeatureHolder<UiEvent, Wish>>
) : Boundary<UiEvent, ViewModel> {

    private val uiEvents = PublishSubject.create<UiEvent>()
    private val sources = mutableMapOf<Store<*, *>, Observable<Wish>>()

    init {
        featureHolders.forEach { connectFeature(it.feature, it.uiEventMapper) }
    }

    private fun connectFeature(feature:  Store<*, Wish>, uiEventMapper: (UiEvent) -> Wish?) {
        val uiSource = createUiSource(uiEventMapper)
        feature.connectSource(uiSource)
        sources[feature] = uiSource
    }

    private fun createUiSource(uiEventMapper: (UiEvent) -> Wish?): Observable<Wish> =
        uiEvents.flatMap {
                uiEventMapper(it)?.let { just(it) } ?: empty()
        }

    override fun onUiEvent(uiEvent: UiEvent) {
        uiEvents.onNext(uiEvent)
    }

    override fun dispose() {
        featureHolders.forEach { disconnectFeature(it.feature) }
    }

    private fun disconnectFeature(feature:  Store<*, Wish>) {
        feature.disconnectSource(sources[feature]!!)
    }

    override fun isDisposed(): Boolean =
        featureHolders.fold(true) { acc, featureHolder ->
            acc && featureHolder.feature.isDisposed
        }

    /**
     * TODO
     */
    class FeatureHolder<UiEvent : Any, Wish : Any>(
        val feature: Store<*, Wish>,
        val uiEventMapper: (UiEvent) -> Wish?
    )
}
