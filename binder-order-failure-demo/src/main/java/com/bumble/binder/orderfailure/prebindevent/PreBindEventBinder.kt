package com.bumble.binder.orderfailure.prebindevent

import com.badoo.binder.using
import com.badoo.mvicore.android.AndroidBindings

class MainActivityBindings(
    view: PreBindEventActivity,
    private val feature1: Feature1
) : AndroidBindings<PreBindEventActivity>(view) {

    override fun setup(view: PreBindEventActivity) {
        binder.bind(feature1 to view using object : (Feature1.State) -> ViewModel() {
            override fun invoke(p1: Feature1.State): ViewModel {
                return ViewModel(p1.text, true)
            }
        })
        binder.bind(view to feature1 using UiEventTransformer1)
    }
}

object UiEventTransformer1 : (UiEvent) -> Feature1.Wish? {

    override fun invoke(uiEvent: UiEvent): Feature1.Wish? =
        when (uiEvent) {
            UiEvent.InitialEvent -> Feature1.Wish.Wish2
            UiEvent.SecondEvent -> null
        }
}
