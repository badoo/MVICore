package com.badoo.mvicore.plugin

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ToolWindowFactory: ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val textField = ConsoleViewImpl(project, true)

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(textField, "", false)
        toolWindow.contentManager.addContent(content)
    }

}
