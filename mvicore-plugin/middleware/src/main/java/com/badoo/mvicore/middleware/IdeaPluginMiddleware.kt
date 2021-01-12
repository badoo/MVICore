package com.badoo.mvicore.middleware

import com.badoo.binder.Connection
import com.badoo.binder.middleware.base.Middleware
import io.reactivex.rxjava3.functions.Consumer

class IdeaPluginMiddleware<Out: Any, In: Any>(
    wrapped: Consumer<In>,
    private val store: EventStore
): Middleware<Out, In>(wrapped) {

    override fun onBind(connection: Connection<Out, In>) {
        super.onBind(connection)
        store.onBind(connection)
    }

    override fun onElement(connection: Connection<Out, In>, element: In) {
        super.onElement(connection, element)
        store.onElement(connection, element)
    }

    override fun onComplete(connection: Connection<Out, In>) {
        super.onComplete(connection)
        store.onComplete(connection)
    }

    interface EventStore {
        fun onBind(connection: Connection<out Any, out Any>)
        fun <T: Any> onElement(connection: Connection<out Any, out Any>, element: T)
        fun onComplete(connection: Connection<out Any, out Any>)
    }
}
