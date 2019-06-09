package com.badoo.mvicore.plugin.ui

import com.google.gson.JsonElement
import java.util.Enumeration
import javax.swing.tree.TreeNode

class JsonRootNode(values: List<JsonElement>) : TreeNode {
        private val children = values.map { JsonTreeNode(it.asJsonObject.keySet().first(), it.child, this) }

        override fun isLeaf(): Boolean = children.isEmpty()

        override fun getChildCount(): Int = children.size

        override fun getParent(): TreeNode? = null

        override fun getAllowsChildren() = true

        override fun getChildAt(i: Int): TreeNode = children[i]

        override fun getIndex(treeNode: TreeNode): Int = children.indexOf(treeNode)

        override fun children() = object : Enumeration<JsonTreeNode> {
            private val iter = children.iterator()
            override fun hasMoreElements(): Boolean = iter.hasNext()
            override fun nextElement(): JsonTreeNode = iter.next()
        }

        override fun toString(): String = ""

        private val JsonElement.child: JsonElement
            get() {
                val key = this.asJsonObject.keySet().first()
                return this.asJsonObject[key]
            }
    }
