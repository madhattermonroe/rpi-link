package com.github.madhattermonroe.rpilink

import com.github.madhattermonroe.rpilink.remote.RemoteOperations
import com.github.madhattermonroe.rpilink.remote.SSHManager
import com.github.madhattermonroe.rpilink.state.ConnectionSettingsState
import com.github.madhattermonroe.rpilink.ui.ConnectionSettingsConfigurable
import com.github.madhattermonroe.rpilink.ui.SystemChart
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.jcraft.jsch.ChannelSftp
import org.knowm.xchart.XChartPanel
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.io.File
import java.io.FileInputStream
import javax.swing.*
import kotlin.concurrent.thread

class Entry : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (ConnectionSettingsState.getInstance(project).isNotValid()) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, ConnectionSettingsConfigurable::class.java)
        }

        if (!SSHManager.isConnected()) {
            SSHManager.connect(project = project)
        }

        val mainPanel = JPanel(BorderLayout())

        val outputArea = SystemChart.createChart()
        val chartPanel = XChartPanel(outputArea)

        mainPanel.add(chartPanel, BorderLayout.CENTER)

        applyTheme(outputArea)

        val bottomPanel = JPanel(BorderLayout())

        val inputPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val commandField = JTextField(30)
        val sendCommandButton = JButton("Send Command")

        val consoleArea = JTextArea(10, 40)
        consoleArea.isEditable = false
        val scrollPane = JBScrollPane(consoleArea)


        sendCommandButton.addActionListener {
            val command = commandField.text
            if (command.isNotEmpty()) {
                val result = RemoteOperations.executeCommand(command)
                appendToConsole(consoleArea, "Command: $command\n")
                appendToConsole(consoleArea, "Result: $result\n\n")
            }
        }

        inputPanel.add(commandField)
        inputPanel.add(sendCommandButton)

        bottomPanel.add(inputPanel, BorderLayout.NORTH)

        bottomPanel.add(scrollPane, BorderLayout.CENTER)

        mainPanel.add(bottomPanel, BorderLayout.SOUTH)


        thread {
            while (true) {
                SystemChart.updateChart(outputArea)

                SwingUtilities.invokeLater {
                    chartPanel.repaint()
                }

                Thread.sleep(500)
            }
        }


        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun uploadFile(file: File, project: Project): Boolean {
        return try {
            val session = SSHManager.getSession()

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

    private fun applyTheme(chart: org.knowm.xchart.XYChart) {
        val background = JBColor.background()
        val foreground = JBColor.foreground()

        chart.styler.chartBackgroundColor = background
        chart.styler.plotBackgroundColor = background
        chart.styler.axisTickLabelsColor = foreground
        chart.styler.legendBackgroundColor = background
    }


    private fun appendToConsole(consoleArea: JTextArea, message: String) {
        SwingUtilities.invokeLater {

            consoleArea.append(message)
            consoleArea.setCaretPosition(consoleArea.document.length)
        }
    }
}