package com.bumble.order.component

object ViewEventToWish : (View.Event) -> Feature.Wish {
    override fun invoke(event: View.Event): Feature.Wish {
        return when (event) {
            View.Event.Point -> Feature.Wish.Point
        }
    }
}

object ViewEventToAnalyticsEvent : (View.Event) -> Analytics.Event {
    override fun invoke(event: View.Event): Analytics.Event {
        return when (event) {
            View.Event.Point -> Analytics.Event.Point
        }
    }
}

object StateToViewModel : (Feature.State) -> View.ViewModel {
    override fun invoke(state: Feature.State): View.ViewModel {
        return View.ViewModel(state.score)
    }
}