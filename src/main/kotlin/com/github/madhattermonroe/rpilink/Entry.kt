package com.github.madhattermonroe.rpilink

import com.github.madhattermonroe.rpilink.remote.RemoteOperations
import com.github.madhattermonroe.rpilink.state.ConnectionSettingsState
import com.github.madhattermonroe.rpilink.ui.ConnectionSettingsConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.ContentFactory
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import org.apache.commons.io.output.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JPanel

class Entry : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (ConnectionSettingsState.getInstance(project).isNotValid()) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, ConnectionSettingsConfigurable::class.java)
        }

        val panel = JPanel()
        val outputArea = JBTextArea(10, 50)
        val refreshButton = JButton("Update monitoring")
        val uploadButton = JButton("Upload File")

        panel.add(outputArea)
        panel.add(refreshButton)
        panel.add(uploadButton)

        refreshButton.addActionListener {
            val status = fetchSystemStatus()
            outputArea.text = status
        }

        uploadButton.addActionListener {
            val fileChooser = JFileChooser()
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val success = uploadFile(file, project)
                Messages.showMessageDialog(
                    if (success) "File has been uploaded successfully!" else "Uploading error!",
                    "File uploading",
                    if (success) Messages.getInformationIcon() else Messages.getErrorIcon()
                )
            }
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun fetchSystemStatus(): String {
        return try {
            val session = RemoteOperations().getSession();

            val command = "top -bn1 | head -10"
            val channel = session?.openChannel("exec") as ChannelExec
            channel.setCommand(command)
            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect()

            while (!channel.isClosed) {
                Thread.sleep(100)
            }
            channel.disconnect()
            session.disconnect()

            outputStream.toString()
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun uploadFile(file: File, project: Project): Boolean {
        return try {
            val session = RemoteOperations().getSession()

            val settings = ConnectionSettingsState.getInstance(project)
            val channel = session?.openChannel("sftp") as ChannelSftp
            channel.connect()
            val remotePath = "/home/${settings.username}/${file.name}"

            channel.put(FileInputStream(file), remotePath)
            channel.disconnect()
            session.disconnect()

            true
        } catch (e: Exception) {
            false
        }
    }
}