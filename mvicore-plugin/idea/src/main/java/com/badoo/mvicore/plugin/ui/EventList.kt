package com.badoo.mvicore.plugin.ui

import com.badoo.mvicore.plugin.model.Event.Item
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.components.JBList
import javax.swing.JList
import javax.swing.ListSelectionModel

class EventList: JBList<Item>() {
    private val items = EventListModel() // what we show
    private var itemSelectionListener: ((Item) -> Unit)? = null

    init {
        model = items
        cellRenderer = CellRenderer()
        addListSelectionListener {
            if (it.valueIsAdjusting || selectedIndex < 0) return@addListSelectionListener
            val item = model.getElementAt(selectedIndex)
            itemSelectionListener?.invoke(item)
        }
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        ListSpeedSearch(this)
    }

    fun add(item: Item) {
        items.add(item)
        scrollToBottom()
    }

    fun clear() {
        clearSelection()
        items.clear()
    }

    fun setFilter(filter: (Item) -> Boolean) {
        clearSelection()
        items.setFilter(filter)
    }

    fun resetFilter() {
        clearSelection()
        items.resetFilter()
    }

    fun setItemSelectionListener(itemSelected: (Item) -> Unit) {
        itemSelectionListener = itemSelected
    }

    private fun scrollToBottom() {
        if (lastVisibleIndex == model.size - 2) {
            ensureIndexIsVisible(model.size - 1)
        }
    }

    private class CellRenderer : ColoredListCellRenderer<Item>() {
        override fun customizeCellRenderer(list: JList<out Item>, value: Item?, index: Int, selected: Boolean, hasFocus: Boolean) {
            append(value?.name().orEmpty())
        }

        private fun Item.name() =
            element.asJsonObject
                ?.keySet()
                ?.first()
    }
}
