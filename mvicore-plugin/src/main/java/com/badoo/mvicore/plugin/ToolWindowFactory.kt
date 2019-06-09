package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.model.Connection
import com.badoo.mvicore.plugin.model.Id
import com.badoo.mvicore.plugin.model.Item
import com.badoo.mvicore.plugin.ui.ConnectionList
import com.badoo.mvicore.plugin.ui.EventList
import com.badoo.mvicore.plugin.ui.JsonRootNode
import com.badoo.mvicore.plugin.utils.mainThreadScheduler
import com.badoo.mvicore.plugin.utils.showError
import com.google.gson.JsonElement
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
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JPanel
import javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.tree.DefaultTreeModel

class ToolWindowFactory : ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(javaClass)
    private val selectedConnections = mutableListOf<Connection>()

    private val events = EventList().apply {
        setItemSelectionListener {
            currentElement.setJsonList(listOf(it.element))
        }
    }
    private val connections = ConnectionList().apply {
        setListener { connection, added ->
            if (added) {
                selectedConnections += connection
            } else {
                selectedConnections -= connection
            }

            events.setFilter {
                if (selectedConnections.isNotEmpty()) {
                    selectedConnections.contains(it.connection)
                } else {
                    true
                }
            }
        }
    }
    private val currentElement = Tree()

    private val disposables = CompositeDisposable()
    private lateinit var eventsObservable: Observable<JsonElement>

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Init events
        eventsObservable = Observable.wrap(SocketObservable(project, 7675))
            .observeOn(mainThreadScheduler)

        // Left
        val left = JBScrollPane(events, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)

        // Right
        val right = JBSplitter(true)
        right.firstComponent = JBScrollPane(connections, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)
        right.secondComponent = JBScrollPane(currentElement, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)

        // The pane
        val splitter = JBSplitter()
        splitter.firstComponent = left
        splitter.secondComponent = right

        val actions = createToolbarActions(project)
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, actions, true)
        val panel = JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.NORTH)
            add(splitter)
        }

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createToolbarActions(project: Project): DefaultActionGroup {
        val actionManager = ActionManager.getInstance()
        val group = DefaultActionGroup()

        val action = object : AnAction(), DumbAware {
            override fun actionPerformed(e: AnActionEvent) {
                disposables.clear()
                connections.clear()
                disposables.add(
                    eventsObservable.subscribe({
                        parseEvent(it)
                    }, {
                        if (it is Exception) {
                            project.showError("Error connecting to device:", it)
                        }
                    })
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
                    if (connection != null) connections.add(connection)
                }
                "data" -> {
                    val connection = it.get("connection").toConnection()
                    val element = it.getAsJsonObject("element")
                    val rootWrapper = JsonObject().apply {
                        val (timestamp, type, value) = element.parse()
                        add("[${timestamp?.dateString()}] : $type", value)
                    }
                    events.add(Item(connection, rootWrapper))
                }
                "complete" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) connections.remove(connection)
                }
            }
        }
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

    private fun Tree.setJsonList(elements: List<JsonElement>) {
        (model as? DefaultTreeModel)?.setRoot(JsonRootNode(elements)) ?: DefaultTreeModel(JsonRootNode(elements)).apply { model = this }
        isRootVisible = false
    }
}
