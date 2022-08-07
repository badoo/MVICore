package com.bumble.order.component

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class View(
    private val events: PublishSubject<Event> = PublishSubject.create<Event>()
) : Consumer<View.ViewModel>, ObservableSource<View.Event> by events {

    data class ViewModel(val score: Int = 0)

    sealed class Event {
        object Point : Event()
    }

    fun score() {
        events.onNext(Event.Point)
    }

    override fun accept(viewModel: ViewModel) {
        println("View ${viewModel.score}")
    }
}