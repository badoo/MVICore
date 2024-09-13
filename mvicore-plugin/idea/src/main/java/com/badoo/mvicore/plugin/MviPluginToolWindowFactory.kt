package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.action.ClearAction
import com.badoo.mvicore.plugin.action.RunAction
import com.badoo.mvicore.plugin.model.ConnectionData
import com.badoo.mvicore.plugin.model.Event.Item
import com.badoo.mvicore.plugin.ui.ConnectionList
import com.badoo.mvicore.plugin.ui.EventList
import com.badoo.mvicore.plugin.ui.JsonRootNode
import com.badoo.mvicore.plugin.utils.mainThreadScheduler
import com.google.gson.JsonElement
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.tree.DefaultTreeModel

class MviPluginToolWindowFactory : ToolWindowFactory {

    private val logger = Logger.getInstance(javaClass)
    private val selectedConnections = mutableListOf<ConnectionData>()

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
                selectedConnections.removeAll { it == connection }
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

        Disposer.register(project) { disposables.clear() }

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
        val group = DefaultActionGroup()

        val run = RunAction(
            project, disposables, connections, eventsObservable, events
        )
        group.add(run)

        return group
    }

    private fun createSidePanelActions() : ActionGroup {
        val group = DefaultActionGroup()

        val clear = ClearAction(
            events,
            currentElement
        )

        group.add(clear)

        return group
    }

    private fun Tree.setItem(item: Item) {
        val node = JsonRootNode(item)
        val model =  (model as? DefaultTreeModel) ?: DefaultTreeModel(node).apply { model = this }
        model.setRoot(node)

        isRootVisible = false
    }
}

fun ActionManager.iconFrom(actionId: String) =
    getAction(actionId).templatePresentation.icon
