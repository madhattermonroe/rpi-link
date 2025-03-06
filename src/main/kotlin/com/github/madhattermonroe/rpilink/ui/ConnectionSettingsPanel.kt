package com.github.madhattermonroe.rpilink.ui

import com.github.madhattermonroe.rpilink.RPIBundle
import com.github.madhattermonroe.rpilink.remote.RemoteOperations
import com.github.madhattermonroe.rpilink.state.ConnectionSettingsState
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

private const val CONF_WIDTH_GROUP = "CredentialsLabel"

class ConnectionSettingsPanel(private val project: Project) {

    private val hostField = JBTextField(COLUMNS_MEDIUM)
    private val usernameField = JBTextField(COLUMNS_MEDIUM)
    private val passwordField = JBPasswordField().also { it.columns = COLUMNS_MEDIUM }
    private val portField: JBTextField = JBTextField(COLUMNS_TINY)

    private var connectionTested = false

    private var testButton = JButton()
    private var disconnectButton = JButton()

    val panel: JPanel = panel {
        row {
            label(RPIBundle.message("connectionConfig.label.host")).widthGroup(CONF_WIDTH_GROUP)
            cell(hostField)

            label(RPIBundle.message("connectionConfig.label.port"))
            portField.text = "22"
            cell(portField)
        }
        row {
            label(RPIBundle.message("connectionConfig.label.username")).widthGroup(CONF_WIDTH_GROUP)
            cell(usernameField)
        }
        row {
            label(RPIBundle.message("connectionConfig.label.password")).widthGroup(CONF_WIDTH_GROUP)
            cell(passwordField)
        }

        row {
            var errorIcon: Cell<JLabel>? = null
            var okIcon: Cell<JLabel>? = null
            var loadingIcon: Cell<JLabel>? = null
            var errorLink: Cell<ActionLink>? = null
            var possibleError: Exception? = null

            testButton = button(RPIBundle.message("settings.RemoteFileAccess.testConnectionButton.text")) {
                errorLink?.visible(false)
                errorIcon?.visible(false)
                okIcon?.visible(false)
                loadingIcon?.visible(true)
                connectionTested = true

                if (isModified()) {
                    saveState()
                }

                if (RemoteOperations().startConnection(
                        hostField.text,
                        portField.text,
                        usernameField.text,
                        String(passwordField.password)
                    )
                ) {
                    loadingIcon?.visible(false)
                    okIcon?.visible(true)
                } else {
                    loadingIcon?.visible(false)
                    errorIcon?.visible(true)
                    errorLink?.visible(true)
                }
            }.component

            loadingIcon = icon(AnimatedIcon.Default.INSTANCE).visible(false)
            errorIcon = icon(AllIcons.General.BalloonError).visible(false)
            okIcon = icon(AllIcons.Actions.Commit).visible(false)
            errorLink = link(RPIBundle.message("settings.RemoteFileAccess.errorDetails.link.text")) {
                val thisErrorLink = errorLink ?: return@link
                showErrorDetailsBalloon(possibleError, thisErrorLink.component)
            }.visible(false)

            disconnectButton = button(RPIBundle.message("settings.RemoteFileAccess.disconnectLink.text")) {
                RemoteOperations().disconnect()
            }.component
        }
    }

    fun reset() {
        val conf = ConnectionSettingsState.getInstance(project)

        hostField.text = conf.host
        portField.text = conf.port.toString()
        usernameField.text = conf.username
    }

    fun saveState() {
        val conf = ConnectionSettingsState.getInstance(project)

        conf.host = hostField.text.trim()
        conf.port = portField.text.toInt()
        conf.username = usernameField.text.trim()
        conf.password = passwordField.password
    }

    fun isModified(): Boolean {
        val conf = ConnectionSettingsState.getInstance(project)
        return connectionTested || conf.host != hostField.text || conf.port.toString() != portField.text || conf.username != usernameField.text || passwordField.password.isNotEmpty()
    }

    private fun showErrorDetailsBalloon(possibleError: Exception?, component: JComponent) {
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
            possibleError?.message ?: "Unknown Error", MessageType.ERROR, null
        ).setShowCallout(false).setHideOnClickOutside(true).setHideOnAction(true).setHideOnFrameResize(true)
            .setHideOnKeyOutside(true).createBalloon()
            .show(RelativePoint.getSouthEastOf(component), Balloon.Position.above)
    }
}