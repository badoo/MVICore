package com.badoo.mvicore.middleware.gc

import com.badoo.binder.Connection
import com.badoo.mvicore.plugin.model.ConnectionData
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.Collections

internal class ConnectionReference(
    ref: Connection<*, *>,
    queue: ReferenceQueue<Connection<*, *>>,
    val data: ConnectionData
): PhantomReference<Connection<*, *>>(ref, queue)

internal class QueueWatcher(
    private val referenceQueue: ReferenceQueue<Connection<*, *>>,
    private val destroyCallback: (ConnectionData) -> Unit
): Thread("mvicore-plugin-queue-watcher") {

    private val references = Collections.synchronizedList(mutableListOf<ConnectionReference>())

    override fun run() {
        while (!isInterrupted) {
            val ref = referenceQueue.remove() as? ConnectionReference
            ref?.let {
                destroyCallback(it.data)
                references.remove(it)
            }
        }
    }

    fun <T : Any, R : Any> add(ref: Connection<T, R>, data: ConnectionData) {
        references.add(
            ConnectionReference(ref, referenceQueue, data)
        )
    }
}
