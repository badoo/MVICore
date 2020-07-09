package com.badoo.mvicore.rx

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import com.badoo.mvicore.common.Observer as MVICoreObserver

fun <T> ObservableSource<T>.toSource(): Source<T> = SourceAdapter(Observable.wrap(this))

fun <T> Source<T>.toObservable(): ObservableSource<T> = ObservableAdapter(this)

internal class SourceAdapter<T>(internal val delegate: Observable<T>): Source<T> {
    override fun connect(observer: MVICoreObserver<T>): Cancellable =
        DisposableAdapter(
            delegate.subscribe(
                observer::accept,
                observer::onError,
                observer::onComplete,
                { observer.onSubscribe(DisposableAdapter(it)) }
            )
        )

    override fun equals(other: Any?): Boolean =
        other is SourceAdapter<*> && delegate == other.delegate

    override fun hashCode(): Int =
        delegate.hashCode()

    override fun toString(): String =
        delegate.toString()
}

internal class DisposableAdapter(private val delegate: Disposable): Cancellable {
    override fun cancel() {
        delegate.dispose()
    }

    override val isCancelled: Boolean
        get() = delegate.isDisposed
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
