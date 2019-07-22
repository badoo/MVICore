package com.badoo.mvicore.plugin.action

import com.badoo.mvicore.plugin.iconFrom
import com.badoo.mvicore.plugin.ui.EventList
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.ui.treeStructure.Tree

class ClearAction(
    private val events: EventList,
    private val currentElement: Tree
) : AnAction() {
    init {
        templatePresentation.icon = ActionManager.getInstance().iconFrom(IdeActions.CONSOLE_CLEAR_ALL)
    }

    override fun actionPerformed(e: AnActionEvent) {
        events.clear()
        currentElement.model = null
    }
}
