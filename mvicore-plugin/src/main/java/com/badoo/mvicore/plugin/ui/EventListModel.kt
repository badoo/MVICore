package com.badoo.mvicore.plugin.ui

import com.badoo.mvicore.plugin.model.Item
import javax.swing.ListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataEvent.CONTENTS_CHANGED
import javax.swing.event.ListDataEvent.INTERVAL_ADDED
import javax.swing.event.ListDataListener

class EventListModel : ListModel<Item> {
    private val items = mutableListOf<Item>()
    private var filteredItems = emptyList<Item>()
    private var filter: (Item) -> Boolean = DEFAULT_FILTER
    private val dataListeners = mutableListOf<ListDataListener>()

    fun setFilter(filter: (Item) -> Boolean) {
        this.filter = filter
        val oldSize = filteredItems.size
        filteredItems = items.filter(filter)
        dataListeners.forEach {
            it.contentsChanged(
                ListDataEvent(this, CONTENTS_CHANGED, 0, oldSize)
            )
        }
    }

    override fun getElementAt(index: Int): Item =
        filteredItems[index]

    override fun getSize(): Int =
        filteredItems.size

    fun add(item: Item) {
        items.add(item)
        if (filter(item)) {
            filteredItems = filteredItems + item
            dataListeners.forEach {
                it.intervalAdded(
                    ListDataEvent(this, INTERVAL_ADDED, filteredItems.lastIndex, filteredItems.lastIndex)
                )
            }
        }
    }

    fun clear() {
        items.clear()
        val oldSize = filteredItems.size
        filteredItems = emptyList()

        dataListeners.forEach {
            it.contentsChanged(
                ListDataEvent(this, CONTENTS_CHANGED, 0, oldSize)
            )
        }
    }

    fun resetFilter() {
        setFilter(DEFAULT_FILTER)
    }

    override fun addListDataListener(l: ListDataListener) {
        dataListeners += l
    }

    override fun removeListDataListener(l: ListDataListener) {
        dataListeners -= l
    }

    companion object {
        private val DEFAULT_FILTER: (Item) -> Boolean = { true }
    }
}
