package com.badoo.mvicore.plugin.ui

import com.badoo.mvicore.plugin.model.Item
import com.badoo.mvicore.plugin.utils.toTreeNode
import com.google.gson.JsonElement
import java.util.Enumeration
import javax.swing.tree.TreeNode

class JsonRootNode(value: Item) : TreeNode {
    private val children = listOf(
        value.connection.toTreeNode(),
        JsonTreeNode(value.element.childKey, value.element.asJsonObject[value.element.childKey], this)
    )

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode? = null

    override fun getAllowsChildren() = true

    override fun getChildAt(i: Int): TreeNode = children[i]

    override fun getIndex(treeNode: TreeNode): Int = children.indexOf(treeNode)

    override fun children() = object : Enumeration<TreeNode> {
        private val iter = children.iterator()
        override fun hasMoreElements(): Boolean = iter.hasNext()
        override fun nextElement(): TreeNode = iter.next()
    }

    override fun toString(): String = ""

    private val JsonElement.childKey: String
        get() = this.asJsonObject.keySet().first()
}
