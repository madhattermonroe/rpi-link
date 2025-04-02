package com.github.madhattermonroe.rpilink.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationManager {
    fun showNotification(message: String, type: NotificationType, project: Project?) {
        val notification = Notification("ConnectionNotifications", "Raspberry Pi SSH", message, type)
        Notifications.Bus.notify(notification, project)
    }
}