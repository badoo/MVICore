package com.bumble.binder.orderfailure.prebindevent

import com.badoo.binder.using
import com.badoo.mvicore.android.AndroidBindings
import com.bumble.binder.orderfailure.combineLatest

class MainActivityBindings(
    view: PreBindEventActivity,
    private val feature1: Feature1,
    private val feature2: Feature2
) : AndroidBindings<PreBindEventActivity>(view) {

    override fun setup(view: PreBindEventActivity) {
        binder.bind(combineLatest(feature1, feature2) to view using ViewModelTransformer)
        binder.bind(view to feature1 using UiEventTransformer1)
        binder.bind(view to feature2 using UiEventTransformer2)
    }
}

object UiEventTransformer1 : (UiEvent) -> Feature1.Wish? {

    override fun invoke(uiEvent: UiEvent): Feature1.Wish? =
        when (uiEvent) {
            UiEvent.InitialEvent -> Feature1.Wish.Wish2
            UiEvent.SecondEvent -> null
        }
}

object UiEventTransformer2 : (UiEvent) -> Feature2.Wish? {

    override fun invoke(uiEvent: UiEvent): Feature2.Wish? =
        when (uiEvent) {
            UiEvent.InitialEvent -> null
            UiEvent.SecondEvent -> Feature2.Wish.Wish1
        }
}

object ViewModelTransformer : (Pair<Feature1.State, Feature2.State>) -> ViewModel {

    override fun invoke(pair: Pair<Feature1.State, Feature2.State>): ViewModel =
        ViewModel(pair.first.text, pair.second.actionEnabled)
}
