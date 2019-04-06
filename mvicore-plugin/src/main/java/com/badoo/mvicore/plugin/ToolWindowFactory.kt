package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.model.Connection
import com.badoo.mvicore.plugin.model.Id
import com.badoo.mvicore.plugin.utils.mainThreadScheduler
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class ToolWindowFactory: ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(javaClass)

    private lateinit var connections: JBList<Connection>

    private val disposables = CompositeDisposable()
    private val activeConnections = ArrayList<Connection>()
    private val eventsObservable =
        Observable.wrap(SocketObservable("localhost", 7675))
            .observeOn(mainThreadScheduler)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        connections = JBList(activeConnections)
        connections.installCellRenderer { connection: Connection ->
            val rootNode = DefaultMutableTreeNode(connection.name)
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

            Tree(rootNode)
        }

        val actions = createToolbarActions()
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, actions, true)
        val panel = JPanel(BorderLayout()).also {
            it.add(toolbar.component, BorderLayout.NORTH)
        }
        panel.add(connections)

        val splitter = JBSplitter()
        splitter.firstComponent = panel
        splitter.secondComponent = JBList(activeConnections)

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
                "complete" -> {
                    val connection = it.get("connection").toConnection()
                    if (connection != null) activeConnections.remove(connection)
                }
            }
        }
        connections.model = DefaultListModel<Connection>().apply {
            activeConnections.forEach { addElement(it) }
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
}
