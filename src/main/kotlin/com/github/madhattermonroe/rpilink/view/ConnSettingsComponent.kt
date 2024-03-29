package com.github.madhattermonroe.rpilink.view

import com.github.madhattermonroe.rpilink.core.RPIBundle
import com.github.madhattermonroe.rpilink.core.connsettings.ConnSettingsState
import com.intellij.collaboration.async.CompletableFutureUtil.submitIOTask
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

private const val CONF_WIDTH_GROUP = "CredentialsLabel"
private const val PASSWORD_AUTH = "Password"
private const val KEY_PAIR_AUTH = "Key Pair"

class ConnSettingsComponent(private val project: Project) {

    private val hostField = JBTextField(COLUMNS_MEDIUM)
    private val usernameField = JBTextField(COLUMNS_MEDIUM)
    private val passwordField = JBPasswordField().also { it.columns = COLUMNS_MEDIUM }
    private val passphraseField = JBPasswordField().also { it.columns = COLUMNS_MEDIUM }
    private val portField: JBTextField = JBTextField(COLUMNS_TINY)

    private var testConnectionButton: JButton? = null
    private var disconnectLink: ActionLink? = null
    private var authenticationTypeComboBox: ComboBox<String>? = null
    private var certificateTextField: TextFieldWithBrowseButton? = null

    private var connectionTested = false

    val panel: JPanel

    init {
        panel = panel {
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
                label(RPIBundle.message("connectionConfig.authType.text")).widthGroup(CONF_WIDTH_GROUP)
                authenticationTypeComboBox = comboBox(listOf(PASSWORD_AUTH, KEY_PAIR_AUTH)).component
            }
            row {
                label(RPIBundle.message("connectionConfig.authType.pk.file")).widthGroup(CONF_WIDTH_GROUP)
                certificateTextField = textFieldWithBrowseButton().columns(COLUMNS_MEDIUM).component
            }.visibleIf(comboBoxPredicate(KEY_PAIR_AUTH))
            row {
                label(RPIBundle.message("connectionConfig.authType.pk.passphrase")).widthGroup(CONF_WIDTH_GROUP)
                cell(passphraseField)
            }.visibleIf(comboBoxPredicate(KEY_PAIR_AUTH))
            row {
                label(RPIBundle.message("connectionConfig.label.password")).widthGroup(CONF_WIDTH_GROUP)
                cell(passwordField)
            }.visibleIf(comboBoxPredicate(PASSWORD_AUTH))

            row {
                var errorIcon: Cell<JLabel>? = null
                var okIcon: Cell<JLabel>? = null
                var loadingIcon: Cell<JLabel>? = null
                var errorLink: Cell<ActionLink>? = null
                var possibleError: Exception? = null

                testConnectionButton =
                    button(RPIBundle.message("settings.RemoteFileAccess.testConnectionButton.text")) {
                        errorLink?.visible(false)
                        errorIcon?.visible(false)
                        okIcon?.visible(false)
                        loadingIcon?.visible(true)
                        connectionTested = true

                        if (isModified()) {
                            saveState()
                        }

                        val remoteOperations = RemoteOperations.getInstance(project)
                        // try to connect
                        ProgressManager.getInstance().submitIOTask(EmptyProgressIndicator()) {
                            remoteOperations.initSilently()
                        }.handleOnEdt(ModalityState.defaultModalityState()) { possibleConnectionError, _ ->
                            loadingIcon?.visible(false)
                            if (possibleConnectionError == null) {
                                okIcon?.visible(true)
                            } else {
                                possibleError = possibleConnectionError
                                errorIcon?.visible(true)
                                errorLink?.visible(true)
                            }
                        }
                    }.component
                disconnectLink = link(RPIBundle.message("settings.RemoteFileAccess.disconnectLink.text")) {
//                    disconnect()
                }.component

                loadingIcon = icon(AnimatedIcon.Default.INSTANCE).visible(false)
                errorIcon = icon(AllIcons.General.BalloonError).visible(false)
                okIcon = icon(AllIcons.Actions.Commit).visible(false)
                errorLink = link(RPIBundle.message("settings.RemoteFileAccess.errorDetails.link.text")) {
                    val thisErrorLink = errorLink ?: return@link
                    showErrorDetailsBalloon(possibleError, thisErrorLink.component)
                }.visible(false)
            }
        }
    }

    fun saveState() {
        val conf = ConnSettingsState.getInstance(project)

        conf.host = hostField.text.trim()
        conf.port = portField.text.toInt()
        conf.username = usernameField.text.trim()

        conf.authenticationType = if (authenticationTypeComboBox?.item == PASSWORD_AUTH) {
            conf.password = passwordField.password
            ConnSettingsState.AuthenticationType.PASSWORD
        } else {
            conf.password = passphraseField.password
            conf.certificateLocation = certificateTextField?.text?.trim() ?: ""
            ConnSettingsState.AuthenticationType.KEYPAIR
        }
    }

    fun isModified(): Boolean {
        val conf = ConnSettingsState.getInstance(project)
        return connectionTested || conf.host != hostField.text || conf.port.toString() != portField.text || conf.username != usernameField.text || conf.certificateLocation != certificateTextField?.text || authenticationTypeComboBoxChanged(
            conf
        ) || passwordField.password.isNotEmpty()
    }

    private fun authenticationTypeComboBoxChanged(conf: ConnSettingsState): Boolean {
        return conf.authenticationType == ConnSettingsState.AuthenticationType.KEYPAIR && authenticationTypeComboBox?.item == PASSWORD_AUTH || conf.authenticationType == ConnSettingsState.AuthenticationType.PASSWORD && authenticationTypeComboBox?.item == KEY_PAIR_AUTH
    }

    private fun comboBoxPredicate(value: String): ComponentPredicate {
        return object : ComponentPredicate() {
            override fun addListener(listener: (Boolean) -> Unit) {
                authenticationTypeComboBox?.addActionListener { listener(invoke()) }
            }

            override fun invoke(): Boolean {
                return authenticationTypeComboBox?.item == value
            }
        }
    }

    private fun showErrorDetailsBalloon(possibleError: Exception?, component: JComponent) {
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
            possibleError?.message ?: "Unknown Error", MessageType.ERROR, null
        ).setShowCallout(false).setHideOnClickOutside(true).setHideOnAction(true).setHideOnFrameResize(true)
            .setHideOnKeyOutside(true).createBalloon()
            .show(RelativePoint.getSouthEastOf(component), Balloon.Position.above)
    }
}