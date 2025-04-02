package com.github.madhattermonroe.rpilink.remote

import com.github.madhattermonroe.rpilink.state.ConnectionSettingsState
import com.github.madhattermonroe.rpilink.util.NotificationManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

object SSHManager {

    private var session: Session? = null

    fun connect(project: Project): Boolean {
        val settings = ConnectionSettingsState.getInstance(project)

        return try {
            val jsch = JSch()
            session = jsch.getSession(settings.username, settings.host, settings.port).apply {
                setPassword(settings.password)
                setConfig("StrictHostKeyChecking", "no")
                connect(5000)
            }
            NotificationManager.showNotification("Successfully connected to RPi", NotificationType.INFORMATION, project)
            true
        } catch (e: Exception) {
            session = null
            NotificationManager.showNotification("Connection error: ${e.message}", NotificationType.ERROR, project)
            false
        }
    }

    fun isConnected(): Boolean {
        return session?.isConnected == true
    }

    fun getSession(): Session? {
        return session
    }

    fun disconnect() {
        session?.disconnect()
        session = null
    }
}