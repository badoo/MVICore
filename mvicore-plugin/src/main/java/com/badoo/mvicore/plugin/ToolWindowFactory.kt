package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.model.Connection
import com.badoo.mvicore.plugin.model.Id
import com.badoo.mvicore.plugin.model.Item
import com.badoo.mvicore.plugin.utils.mainThreadScheduler
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Enumeration
import java.util.LinkedList
import javax.swing.JPanel
import javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
import javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class ToolWindowFactory : ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(javaClass)
    private val itemsList = LinkedList<Item>()

    private val connections = Tree()
    private val elements = Tree()
    private val elementScroll = JBScrollPane(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER).apply {
        setViewportView(elements)
    }
    private val search = TreeSpeedSearch(connections)

    private val disposables = CompositeDisposable()
    private val activeConnections = ArrayList<Connection>()
    private lateinit var eventsObservable: Observable<JsonElement>

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val actions = createToolbarActions()
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, actions, true)
        val panel = JPanel(BorderLayout()).also {
            it.add(toolbar.component, BorderLayout.NORTH)
        }
        panel.add(
            JBScrollPane(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER).apply {
                setViewportView(connections)
            }
        )

        eventsObservable = Observable.wrap(SocketObservable(project, "localhost", 7675))
            .observeOn(mainThreadScheduler)

//        connections.addTreeSelectionListener {
//            var path = it.path
//            while (path != null && (path.lastPathComponent as DefaultMutableTreeNode).userObject !is Connection) path = path.parentPath
//            if (path == null) return@addTreeSelectionListener
//
//            val connection = (path.lastPathComponent as DefaultMutableTreeNode).userObject as Connection
//            elements.setJsonList(itemsList.filter { it.connection == connection }.map { it.element })
//        }

        val splitter = JBSplitter()
        splitter.firstComponent = panel
        splitter.secondComponent = elementScroll

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(splitter, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createToolbarActions(): DefaultActionGroup {
        val actionManager = ActionManager.getInstance()
        val group = DefaultActionGroup()

        val action = object : AnAction(), DumbAware {
            override fun actionPerformed(e: AnActionEvent) {
                disposables.clear()
                disposables.add(
                    eventsObservable.subscribe {
                        parseEvent(it)
                    }
                )
            }
        }
        action.copyFrom(actionManager.getAction(IdeActions.ACTION_DEFAULT_RUNNER))
        group.add(action)

        return group
    }

    private fun parseEvent(it: JsonElement) {
        when (it) {
            is JsonObject -> when (val type = it.get("type").asString) {
                "bind" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) activeConnections.add(connection)
                }
                "data" -> {
                    val connection = it.get("connection").toConnection()
                    val element = it.getAsJsonObject("element")
                    val rootWrapper = JsonObject().apply {
                        val (timestamp, type, value) = element.parse()
                        add("${timestamp?.dateString()} $type", value)
                    }
                    itemsList.add(
                        Item(connection, rootWrapper)
                    )
                    elements.setJsonList(itemsList.map { it.element })
                }
                "complete" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) activeConnections.remove(connection)
                }
            }
        }
        connections.setConnectionList(activeConnections)
    }

    private fun JsonElement.toConnection() = when (this) {
        is JsonObject -> Connection(
            from = nullableString("from")?.toId(),
            to = nullableString("to")?.toId(),
            name = nullableString("name")
        )
        else -> null
    }

    private fun JsonElement.parse(): Triple<Long?, String?, JsonElement> = when (this) {
        is JsonObject -> if (isWrapper) {
            val obj = this
            val type = obj.remove(TYPE).asString
            val primitive = obj.remove(VALUE)
            val timestamp = obj.remove(TIMESTAMP)?.asLong

            if (primitive == null) {
                this.entrySet().map {
                    val (_, type, obj) = it.value.parse()
                    Triple(it.key, type, obj)
                }.forEach {
                    remove(it.first)
                    add("${it.first} (${it.second})", it.third)
                }
            }

            Triple(timestamp, type, primitive ?: this)
        } else {
            Triple(null, null, this)
        }
        else -> Triple(null, null, this)
    }

    private val TYPE = "\$type"
    private val VALUE = "\$value"
    private val TIMESTAMP = "\$timestamp"

    private val JsonElement.isWrapper: Boolean
        get() = isJsonObject && this.asJsonObject.get(TYPE) != null

    private fun Long.dateString() =
        SimpleDateFormat("HH:mm:ss").format(Date(this))

    private fun String.toId(): Id? {
        val parts = split("@")
        if (parts.size != 2) return Id(name = this, hash = null)
        return Id(
            name = parts[0],
            hash = parts[1]
        )
    }

    private fun JsonObject.nullableString(key: String) =
        get(key)?.asString?.let {
            if (it == "null") null else it
        }

    private fun Tree.setConnectionList(connections: List<Connection>) {
        val root = DefaultMutableTreeNode()

        for (connection in connections) {
            val rootNode = DefaultMutableTreeNode(connection)
            connection.from?.shortName?.let {
                rootNode.add(
                    DefaultMutableTreeNode("from: $it")
                )
            }

            connection.to?.shortName?.let {
                rootNode.add(
                    DefaultMutableTreeNode("to: $it")
                )
            }

            root.add(rootNode)
        }


        isRootVisible = false
    }

    private fun Tree.setJsonList(elements: List<JsonElement>) {
        val isScrolledToBottom = (elementScroll.verticalScrollBar.value + elementScroll.verticalScrollBar.model.extent >= elementScroll.verticalScrollBar.maximum)

        (model as? DefaultTreeModel)?.setRoot(JsonRootNode(elements)) ?: DefaultTreeModel(JsonRootNode(elements)).apply { model = this }

        elementScroll.verticalScrollBar.revalidate()

        if (isScrolledToBottom) {
            elementScroll.verticalScrollBar.value = elementScroll.verticalScrollBar.maximum
            elementScroll.verticalScrollBar.revalidate()
        }
        isRootVisible = false
    }

    private class JsonRootNode(values: List<JsonElement>) : TreeNode {
        private val children = values.map { JsonTreeNode(it.asJsonObject.keySet().first(), it.child, this) }

        override fun isLeaf(): Boolean = children.size == 0

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

    private class JsonTreeNode(private val name: String, private val element: JsonElement, private val parent: TreeNode) : TreeNode {
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
}
