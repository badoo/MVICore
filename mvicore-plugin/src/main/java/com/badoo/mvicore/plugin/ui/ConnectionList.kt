package com.badoo.mvicore.plugin.ui

import com.badoo.mvicore.plugin.model.Connection
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import java.util.ArrayList
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class ConnectionList : Tree() {
    private val connectionsList = ArrayList<Connection>()
    private val rootNode = DefaultMutableTreeNode()
    private var listener: ((Connection, added: Boolean) -> Unit)? = null

    init {
        model = DefaultTreeModel(rootNode)
        isRootVisible = false

        addTreeSelectionListener(::selectionListener)

        TreeSpeedSearch(this)
    }

    private fun selectionListener(event: TreeSelectionEvent) {
        // TODO: make more understandable
        event.paths.forEachIndexed { i, eventPath ->
            var path = eventPath
            while (path != null && (path.lastPathComponent as DefaultMutableTreeNode).userObject !is Connection) {
                path = path.parentPath
            }
            if (path == null) return

            if (event.isAddedPath(i) && path != eventPath) {
                removeSelectionPath(event.path)
                addSelectionPath(path)
                return
            }

            if (path == eventPath) {
                val connection = (path.lastPathComponent as DefaultMutableTreeNode).userObject as Connection
                listener?.invoke(connection, event.isAddedPath(i))
            }
        }
    }

    fun add(connection: Connection) {
        connectionsList.add(connection)

        val connectionNode = DefaultMutableTreeNode(connection)
        connection.from?.shortName?.let {
            connectionNode.add(
                DefaultMutableTreeNode("from: $it")
            )
        }

        connection.to?.shortName?.let {
            connectionNode.add(
                DefaultMutableTreeNode("to: $it")
            )
        }
        connectionNode.userObject = connection

        rootNode.add(connectionNode)
        reload()
    }

    fun remove(connection: Connection) {
        val childCount = rootNode.childCount
        for (i in 0 until childCount) {
            val node = rootNode.getChildAt(i) as DefaultMutableTreeNode
            if (node.userObject == connection) {
                rootNode.remove(i)
                reload()
                return
            }
        }
    }

    fun setListener(listener: (Connection, added: Boolean) -> Unit) {
        this.listener = listener
    }

    fun clear() {
        rootNode.removeAllChildren()
        reload()
    }

    private fun reload() {
        (model as DefaultTreeModel).reload()
    }
}
