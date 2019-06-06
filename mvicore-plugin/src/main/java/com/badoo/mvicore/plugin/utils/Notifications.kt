package com.badoo.mvicore.plugin.utils

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.SwingUtilities

fun Project.showError(message: String, ex: Exception? = null) {
    val exception = ex?.convertToString() ?: ""
    val notification = notificationGroup.createNotification(message + "\n" + exception, NotificationType.ERROR)
    SwingUtilities.invokeLater {
        Notifications.Bus.notify(notification, this)
    }
}

private fun Exception.convertToString(): String = StringWriter().also {
    printStackTrace(PrintWriter(it))
}.toString()


private val notificationGroup = NotificationGroup("MVICore", NotificationDisplayType.BALLOON, true)
