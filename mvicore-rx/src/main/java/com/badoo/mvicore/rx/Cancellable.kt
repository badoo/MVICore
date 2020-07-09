package com.badoo.mvicore.rx

import com.badoo.mvicore.common.Cancellable
import io.reactivex.disposables.Disposable

fun Cancellable.toDisposable() = object : Disposable {
    override fun isDisposed(): Boolean =
        isCancelled

    override fun dispose() {
        cancel()
    }

}
