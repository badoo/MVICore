package com.badoo.mvicore.boundary

import com.badoo.mvicore.core.Store
import io.reactivex.Observable

/**
 * Gluing layer between UI and Feature
 *
 * TODO
 */
abstract class SingleFeatureBoundary<in UiEvent : Any, Wish : Any, State : Any, ViewModel : Any>(
    feature: Store<State, Wish>,
    uiEventMapper: (UiEvent) -> Wish,
    private val viewModelTransformer: ((State) -> ViewModel)?
) : MultiFeatureBoundary<UiEvent, Wish, ViewModel>(
    featureHolders = listOf(
        FeatureHolder(
            feature = feature,
            uiEventMapper = uiEventMapper
        )
    )
) {
    override val viewModels: Observable<ViewModel> = feature.states.map {
        viewModelTransformer?.invoke(it)
    }
}
