package com.badoo.mvicore.plugin.ui

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import java.util.Enumeration
import javax.swing.tree.TreeNode

class JsonTreeNode(
    private val name: String,
    private val element: JsonElement,
    private val parent: TreeNode
) : TreeNode {
    private val children = ArrayList<JsonTreeNode>()

    init {
        when {
            element.isJsonObject -> {
                val obj = element.asJsonObject

                for ((key, value) in obj.entrySet()) {
                    if (value !is JsonNull) {
                        children.add(JsonTreeNode(key, value, this))
                    }
                }
            }

            element.isJsonArray -> {
                val array = element.asJsonArray
                for (i in 0 until array.size()) {
                    children.add(JsonTreeNode("[$i]", array.get(i), this))
                }
            }
        }
    }

    override fun isLeaf(): Boolean = children.size == 0

    override fun getChildCount(): Int = children.size

    override fun getParent(): TreeNode = parent

    override fun getAllowsChildren() = element.isJsonObject || element.isJsonArray

    override fun getChildAt(i: Int): TreeNode = children[i]

    override fun getIndex(treeNode: TreeNode): Int = children.indexOf(treeNode)

    override fun children() = object : Enumeration<JsonTreeNode> {
        private val iter = children.iterator()
        override fun hasMoreElements(): Boolean = iter.hasNext()
        override fun nextElement(): JsonTreeNode = iter.next()
    }

    override fun toString(): String = if (allowsChildren) name else "$name: $element"
}

