package com.badoo.mvicore.feature.internal

import io.reactivex.disposables.Disposable

internal class Disposables : Disposable {

    private var disposables: ArrayList<Disposable>? = ArrayList()

    override fun isDisposed(): Boolean = disposables == null

    override fun dispose() {
        disposables?.forEach(Disposable::dispose)
        disposables = null
    }

    fun add(disposable: Disposable) {
        disposables?.apply {
            removeAll(Disposable::isDisposed)
            add(disposable)
        }
    }
}
