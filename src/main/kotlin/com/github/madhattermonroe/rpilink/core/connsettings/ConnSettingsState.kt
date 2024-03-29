package com.github.madhattermonroe.rpilink.core.connsettings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project


@Service(Service.Level.PROJECT)
@State(
    name = "com.github.madhattermonroe.rpilink.core.config.ConnectionConfigState",
    storages = [Storage("RPILinkPlugin.xml", roamingType = RoamingType.DISABLED)]
)
class ConnSettingsState :
    SimplePersistentStateComponent<ConnSettingsState.ConnectionParams>(ConnectionParams()) {
    class ConnectionParams : BaseState() {
        var host by string()
        var port: Int by property(22)
        var username by string()
        var authenticationType by enum<AuthenticationType>(AuthenticationType.PASSWORD)
        var certificateLocation by string()
    }

    enum class AuthenticationType {
        PASSWORD,
        KEYPAIR,
    }

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

    var authenticationType: AuthenticationType
        get() = state.authenticationType
        set(value) {
            state.authenticationType = value
        }

    var certificateLocation: String
        get() = state.certificateLocation ?: ""
        set(value) {
            state.certificateLocation = value
        }

    var password: CharArray = CharArray(0)

    fun isValid(): Boolean {
        return host.isNotEmpty() && username.isNotEmpty()
    }

    fun isNotValid(): Boolean {
        return !isValid()
    }
    companion object {
        fun getInstance(project: Project): ConnSettingsState = project.service()
    }
}