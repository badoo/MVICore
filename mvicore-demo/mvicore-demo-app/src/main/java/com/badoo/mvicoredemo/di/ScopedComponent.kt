package com.badoo.mvicoredemo.di

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

abstract class ScopedComponent<T : Any> {

    protected var component: T? = null
        private set

    private val subScopes: MutableSet<WeakReference<ScopedComponent<*>>> = mutableSetOf()

    private val disposables = CompositeDisposable()

    protected abstract fun create(): T

    protected open fun T.disposables(): Array<Disposable> = emptyArray()

    fun initialize() {
        get()
    }

    fun get(): T {
        if (component == null) {
            component = create().apply { disposables().forEach { disposables.add(it) } }
        }

        return component!!
    }

    fun dependAndGet(subScope: ScopedComponent<*>): T? {
        subScopes.add(WeakReference(subScope))
        return get()
    }

    open fun destroy() {
        component = null
        subScopes.forEach { it.get()?.destroy() }
        subScopes.clear()
        disposables.clear()
    }
}
