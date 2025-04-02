package com.github.madhattermonroe.rpilink.state

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project


@Service(Service.Level.PROJECT)
@State(
    name = "com.github.madhattermonroe.rpilink.state.ConnectionConfigState",
    storages = [Storage("RPILinkPlugin.xml", roamingType = RoamingType.DISABLED)]
)
class ConnectionSettingsState : SimplePersistentStateComponent<ConnectionParams>(ConnectionParams()) {

    var host: String
        get() = state.host ?: ""
        set(value) {
            state.host = value
        }

    var port: Int
        get() = state.port
        set(value) {
            state.port = value
        }

    var username: String
        get() = state.username ?: ""
        set(value) {
            state.username = value
        }

    var password: String
        get() = state.password ?: ""
        set(value) {
            state.password = value
        }

    var autoConnect: Boolean
        get() = state.autoConnect ?: false
        set(value) {
            state.autoConnect = value
        }

    private fun isValid(): Boolean {
        return host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
    }

    fun isNotValid(): Boolean {
        return !isValid()
    }

    companion object {
        fun getInstance(project: Project): ConnectionSettingsState = project.service()
    }
}