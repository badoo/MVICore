package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.mainThreadScheduler
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.content.ContentFactory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel

class ToolWindowFactory: ToolWindowFactory, DumbAware {

    private val logger = Logger.getInstance(javaClass)

    private lateinit var connections: JBList<String>

    private val disposables = CompositeDisposable()
    private val activeConnections = ArrayList<String>()
    private val eventsObservable =
        Observable.wrap(SocketObservable("localhost", 7675))
            .observeOn(mainThreadScheduler)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        connections = JBList(activeConnections)

        val actions = createToolbarActions()
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.COMMANDER_TOOLBAR, actions, true)
        val panel = JPanel(BorderLayout()).also {
            it.add(toolbar.component, BorderLayout.NORTH)
        }
        panel.add(connections)

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
                "bind" -> activeConnections.add(it.get("connection").toString())
                "complete" -> activeConnections.remove(it.get("connection").toString())
            }
        }
        connections.model = DefaultListModel<String>().apply {
            activeConnections.forEach { addElement(it) }
        }
    }
}
