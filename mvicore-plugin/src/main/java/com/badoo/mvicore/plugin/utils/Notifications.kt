package com.badoo.mvicore.plugin.utils

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import javax.swing.SwingUtilities

fun Project.showError(message: String) {
    val notification = notificationGroup.createNotification(message, NotificationType.ERROR)
    SwingUtilities.invokeLater {
        Notifications.Bus.notify(notification, this)
    }
}

private val notificationGroup = NotificationGroup("MVICore", NotificationDisplayType.BALLOON, true)
