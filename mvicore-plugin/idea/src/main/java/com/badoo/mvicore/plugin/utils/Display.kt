package com.badoo.mvicore.plugin.utils

import com.badoo.mvicore.plugin.model.ConnectionData
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

fun ConnectionData.toTreeNode(): MutableTreeNode {
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
