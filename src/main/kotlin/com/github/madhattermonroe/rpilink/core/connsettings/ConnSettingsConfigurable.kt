package com.github.madhattermonroe.rpilink.core.connsettings

import com.github.madhattermonroe.rpilink.core.RPIBundle
import com.github.madhattermonroe.rpilink.view.ConnSettingsComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class ConnSettingsConfigurable(private val project: Project) : Configurable {

    private var connSettingsComponent: ConnSettingsComponent? = null

    override fun createComponent(): JComponent {
        if (connSettingsComponent == null) {
            connSettingsComponent = ConnSettingsComponent(project)
        }
        return connSettingsComponent?.panel ?: throw IllegalStateException()
    }

    override fun isModified(): Boolean {
        return connSettingsComponent?.isModified() ?: throw IllegalStateException()
    }

//    override fun reset() {
//        connectionSettingsComponent?.reset()
//    }

    override fun apply() {
        connSettingsComponent?.saveState()
//        val remoteOperations = RemoteOperations.getInstance(project)
//        remoteOperations.initWithModalDialogue()
//        notifyUpdateFullTree()
    }

    override fun getDisplayName(): String {
        return RPIBundle.message("settings.RemoteFileAccess.configurable.displayName")
    }
}