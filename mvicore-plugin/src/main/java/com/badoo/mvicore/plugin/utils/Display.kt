package com.badoo.mvicore.plugin.utils

import com.badoo.mvicore.plugin.model.Connection
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

fun Connection.toTreeNode(): MutableTreeNode {
    val connectionNode = DefaultMutableTreeNode(this)
    from?.shortName?.let {
        connectionNode.add(
            DefaultMutableTreeNode("from: $it")
        )
    }

    to?.shortName?.let {
        connectionNode.add(
            DefaultMutableTreeNode("to: $it")
        )
    }
    connectionNode.userObject = this

    return connectionNode
}
