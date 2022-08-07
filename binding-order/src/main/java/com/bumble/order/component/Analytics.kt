package com.bumble.order.component

import io.reactivex.functions.Consumer

class Analytics : Consumer<Analytics.Event> {

    sealed class Event {
        object Point : Event()
    }

    override fun accept(event: Event) {
        when (event) {
            is Event.Point -> println("Analytics: ${event::class.java.simpleName}")
        }
    }
}