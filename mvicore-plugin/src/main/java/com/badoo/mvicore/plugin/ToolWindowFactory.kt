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
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBSplitter
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBList
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
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class ToolWindowFactory : ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(javaClass)
    private val elements = JBList(DefaultListModel<Item>()).apply {
        cellRenderer = object : ColoredListCellRenderer<Item>() {
            override fun customizeCellRenderer(list: JList<out Item>, value: Item?, index: Int, selected: Boolean, hasFocus: Boolean) {
                append(value?.element?.asJsonObject?.keySet()?.first().orEmpty())
            }
        }
        addListSelectionListener {
            val item = this.model.getElementAt(this.selectedIndex)
            currentElement.model = DefaultTreeModel(JsonRootNode(listOf(item.element))).apply { currentElement.isRootVisible = false }
        }
        selectionMode = ListSelectionModel.SINGLE_SELECTION
    }
    private val itemsList = LinkedList<Item>()

    private val connections = Tree()
    private val connectionsList = ArrayList<Connection>()
    private val currentElement = Tree()

    private val search = ListSpeedSearch(elements)
    private val connectionsSearch = TreeSpeedSearch(connections)

    private val disposables = CompositeDisposable()
    private lateinit var eventsObservable: Observable<JsonElement>

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Init events
        eventsObservable = Observable.wrap(SocketObservable(project, "localhost", 7675))
            .observeOn(mainThreadScheduler)

        // Left
        val left = JBScrollPane(elements, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)

        // Right
        val right = JBSplitter(true)
        right.firstComponent = JBScrollPane(connections, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)
        right.secondComponent = JBScrollPane(currentElement, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)

//        connections.addTreeSelectionListener {
//            var path = it.path
//            while (path != null && (path.lastPathComponent as DefaultMutableTreeNode).userObject !is Connection) path = path.parentPath
//            if (path == null) return@addTreeSelectionListener
//
//            val connection = (path.lastPathComponent as DefaultMutableTreeNode).userObject as Connection
//            elements.setJsonList(itemsList.filter { it.connection == connection }.map { it.element })
//        }

        // The pane
        val splitter = JBSplitter()
        splitter.firstComponent = left
        splitter.secondComponent = right

        val actions = createToolbarActions()
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, actions, true)
        val panel = JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.NORTH)
            add(splitter)
        }

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(panel, "", false)
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
                    if (connection != null) connectionsList.add(connection)
                }
                "data" -> {
                    val connection = it.get("connection").toConnection()
                    val element = it.getAsJsonObject("element")
                    val rootWrapper = JsonObject().apply {
                        val (timestamp, type, value) = element.parse()
                        add("[${timestamp?.dateString()}] : $type", value)
                    }
                    itemsList.add(
                        Item(connection, rootWrapper)
                    )
                    (elements.model as DefaultListModel<Item>).clear()
                    itemsList.forEach {
                        (elements.model as DefaultListModel<Item>).addElement(it)
                    }
                }
                "complete" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) connectionsList.remove(connection)
                }
            }
        }
        connections.setConnectionList(connectionsList)
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
            var primitive = obj.remove(VALUE)
            val timestamp = obj.remove(TIMESTAMP)?.asLong

            if (primitive == null) {
                this.entrySet().map {
                    val (_, type, obj) = it.value.parse()
                    Triple(it.key, type, obj)
                }.forEach {
                    remove(it.first)
                    add("${it.first}${it.second?.let {" ($it)"}.orEmpty()}", it.third)
                }
            } else if (primitive.isJsonArray) {
                val array = primitive.asJsonArray
                val result = JsonObject()
                array.mapIndexed { i, el ->
                    val (_, type, obj) = el.parse()
                  Pair(type, obj)
                }.forEachIndexed { i, (type, obj) ->
                    result.add("$i${type?.let {" ($it)"}.orEmpty()}", obj)
                }
                primitive = result
            }

            Triple(timestamp, if (primitive == null) type else null, primitive ?: this)
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

        model = DefaultTreeModel(root)
        isRootVisible = false
    }

    private fun Tree.setJsonList(elements: List<JsonElement>) {
        (model as? DefaultTreeModel)?.setRoot(JsonRootNode(elements)) ?: DefaultTreeModel(JsonRootNode(elements)).apply { model = this }
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
