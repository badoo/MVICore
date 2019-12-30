package com.badoo.mvicore.rx

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.cancellableOf
import com.badoo.mvicore.common.connect
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposables

fun <T> ObservableSource<T>.toSource(): Source<T> = SourceAdapter(Observable.wrap(this))

fun <T> Source<T>.toObservable(): ObservableSource<T> = ObservableAdapter(this)

internal class SourceAdapter<T>(internal val delegate: Observable<T>): Source<T> {
    private val disposable = Disposables.empty()

    override fun connect(sink: Sink<T>): Cancellable {
        val disposable = delegate.subscribe { sink(it) }
        return cancellableOf {
            disposable.dispose()
        }
    }

    override fun equals(other: Any?): Boolean =
        other is SourceAdapter<*> && super.equals(other.delegate)

    override fun hashCode(): Int =
        delegate.hashCode()

    override fun toString(): String =
        delegate.toString()
}

internal class ObservableAdapter<T>(private val delegate: Source<T>): ObservableSource<T> {
    override fun subscribe(observer: Observer<in T>) {
        try {
            val cancellable = delegate.connect { observer.onNext(it) }
            observer.onSubscribe(Disposables.fromAction { cancellable.cancel() })
        } catch (e: Exception) {
            observer.onError(e)
        }
    }
}
