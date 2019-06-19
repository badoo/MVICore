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
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JPanel
import javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.tree.DefaultTreeModel

class ToolWindowFactory : ToolWindowFactory {

    private val logger = Logger.getInstance(javaClass)
    private val selectedConnections = mutableListOf<Connection>()

    private val events = EventList().apply {
        setItemSelectionListener {
            currentElement.setItem(it)
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
        val sideActions = createSidePanelActions()
        val sideActionsBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, sideActions, false)
        val left = JPanel(BorderLayout()).apply {
            add(sideActionsBar.component, BorderLayout.WEST)
            add(JBScrollPane(events, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED))
        }

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

        val run = object : AnAction() {
            init {
                templatePresentation.icon = actionManager.iconFrom(IdeActions.ACTION_DEFAULT_RUNNER)
            }

            override fun actionPerformed(e: AnActionEvent) {
                if (disposables.size() > 0) return
                disposables += eventsObservable.subscribe({
                    parseEvent(it)
                }, {
                    if (it is Exception) {
                        project.showError("Error connecting to device:", it)
                    }
                })
            }
        }

        val stop = object : AnAction() {
            init {
                templatePresentation.icon = actionManager.iconFrom(IdeActions.ACTION_STOP_PROGRAM)
            }

            override fun actionPerformed(e: AnActionEvent) {
                disposables.clear()
                connections.clear()
            }
        }

        group.add(run)
        group.add(stop)

        return group
    }

    private fun createSidePanelActions() : ActionGroup {
        val actionManager = ActionManager.getInstance()
        val group = DefaultActionGroup()

        val clear = object : AnAction() {
            init {
                templatePresentation.icon = actionManager.iconFrom(IdeActions.CONSOLE_CLEAR_ALL)
            }

            override fun actionPerformed(e: AnActionEvent) {
                events.clear()
                currentElement.model = null
            }
        }

        group.add(clear)

        return group
    }

    private fun ActionManager.iconFrom(actionId: String) =
        getAction(actionId).templatePresentation.icon

    private fun parseEvent(it: JsonElement) {
        when (it) {
            is JsonObject -> when (val type = it.get("type").asString) {
                "bind" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) connections.add(connection)
                }
                "data" -> {
                    val connection = it.get("connection").toConnection() ?: return
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
                    it to it.value.parse()
                }.forEach { (entry, result) ->
                    val (key, _) = entry
                    val (_, type, child) = result
                    val typeDesc = type?.let {" ($it)"}.orEmpty()
                    obj.remove(key)
                    obj.add("$key$typeDesc", child)
                }
            } else if (primitive.isJsonArray) {
                val array = primitive.asJsonArray
                val result = JsonObject()
                array.forEachIndexed { i, el ->
                    val (_, type, obj) = el.parse()
                    val typeDesc = type?.let {" ($it)"}.orEmpty()
                    result.add("$i$typeDesc", obj)
                }
                primitive = result
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

    private fun Tree.setItem(item: Item) {
        val node = JsonRootNode(item)
        val model =  (model as? DefaultTreeModel) ?: DefaultTreeModel(node).apply { model = this }
        model.setRoot(node)

        isRootVisible = false
    }
}
