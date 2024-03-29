package com.github.madhattermonroe.rpilink

import com.github.madhattermonroe.rpilink.core.connsettings.ConnSettingsConfigurable
import com.github.madhattermonroe.rpilink.core.connsettings.ConnSettingsState
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class Entry : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val configuration = ConnSettingsState.getInstance(project)

        if (configuration.isNotValid()) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, ConnSettingsConfigurable::class.java)
        }
    }
}