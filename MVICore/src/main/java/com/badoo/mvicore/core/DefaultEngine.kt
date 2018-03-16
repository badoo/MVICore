package com.badoo.mvicore.core

import com.badoo.mvicore.assertOnMainThread
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * TODO
 */
class DefaultEngine<State : Any, Wish : Any, Effect : Any>: Engine<State, Wish, Effect> {
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val sources: MutableMap<Observable<Wish>, Disposable> = mutableMapOf()
    private val wishes = PublishSubject.create<Wish>()
    private lateinit var reducer: Reducer<State, Effect>
    private lateinit var stateSubject: BehaviorSubject<State>

    override fun init(feature: Feature<State, Wish, Effect>) {
        stateSubject = BehaviorSubject.createDefault(feature.initialState)
        reducer = feature.reducer
        feature.middlewares?.forEach { reducer = it.create(reducer) }

        disposables += wishes
                .flatMap {
                    feature.actor.invoke(it, state)
                            .doOnNext {
                                assertOnMainThread()
                                stateSubject.onNext(feature.reducer.invoke(state, it))
                            }
                }
                .subscribe {
                    if (it is News) {
                        feature.newsObserver?.onNext(it)
                    }
                }

        feature.initialSources?.forEach {
            connectSource(it)
        }
    }

    override fun connectSource(source: Observable<Wish>) {
        sources[source] = source.subscribe {
            wishes.onNext(it)
        }
    }

    override fun disconnectSource(source: Observable<Wish>) {
        sources[source]?.dispose()
    }

    override val state: State
        get() = stateSubject.value

    override val states: Observable<State>
        get() = stateSubject

    override fun dispose() {
        disposables.dispose()
        sources.values.forEach {
            it.dispose()
        }
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed &&
        sources.values.fold(true) { acc, disposable ->
            acc && disposable.isDisposed
        }
}
