package com.github.madhattermonroe.rpilink.ui

import com.github.madhattermonroe.rpilink.RPIBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class ConnectionSettingsConfigurable(private val project: Project) : Configurable {

    private var connectionSettingsPanel: ConnectionSettingsPanel? = null

    override fun createComponent(): JComponent {
        if (connectionSettingsPanel == null) {
            connectionSettingsPanel = ConnectionSettingsPanel(project)
        }
        return connectionSettingsPanel?.panel ?: throw IllegalStateException()
    }

    override fun isModified(): Boolean {
        return connectionSettingsPanel?.isModified() ?: throw IllegalStateException()
    }

    override fun reset() {
        connectionSettingsPanel?.reset()
    }

    override fun apply() {
        connectionSettingsPanel?.saveState()
    }

    override fun getDisplayName(): String {
        return RPIBundle.message("settings.RemoteFileAccess.configurable.displayName")
    }
}