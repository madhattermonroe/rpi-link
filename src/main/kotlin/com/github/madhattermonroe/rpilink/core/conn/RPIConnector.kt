package com.github.madhattermonroe.rpilink.core.conn

import com.github.madhattermonroe.rpilink.core.connsettings.ConnSettingsState
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import net.schmizz.sshj.SSHClient
import java.util.concurrent.locks.ReentrantLock

internal const val CONNECTION_TIMEOUT_MS = 10000
internal const val TIMEOUT_MS = 10000

@Service(Service.Level.PROJECT)
class RPIConnector(private val project: Project) : Disposable {

    @Volatile
    private var sshClient: SSHClient? = null

    private val lock = ReentrantLock()

    private fun isInitializedAndConnectedInternal(sshClient: SSHClient?): Boolean {
        return sshClient != null && sshClient.isConnected && sshClient.isAuthenticated
    }
    fun connect(): Exception? {
        val prevSsh1 = sshClient

        if (isInitializedAndConnectedInternal(prevSsh1)) {
            return null
        }
        try {
            lock.lock()
            val prevSsh2 = sshClient

            if (isInitializedAndConnectedInternal(prevSsh2)) {
                return null
            }

            val failReason = tryConnect()
            return if (failReason != null) {
                failReason
            } else {
                val newSshClient = sshClient
                if (newSshClient != null) {
                    val disconnectNotifier = DisconnectNotifier(project)
                    newSshClient.transport.disconnectListener = disconnectNotifier
                    null
                } else {
                    IOException("Fail reason is null, but client is null as well")
                }
            }
        } finally {
            lock.unlock()
        }
    }

    private fun tryConnect() {
        val conf = ConnSettingsState.getInstance(project)
        val host = conf.host
        val port = conf.port
        val username = conf.username
        val password = conf.password

        val sshClientTemp = SSHClient()

        sshClientTemp.connectTimeout = CONNECTION_TIMEOUT_MS
        sshClientTemp.timeout = TIMEOUT_MS
        sshClientTemp.useCompression()
        sshClientTemp.loadKnownHosts()

        sshClientTemp.connect(host, port)

        if (conf.authenticationType == ConnSettingsState.AuthenticationType.PASSWORD)
            sshClientTemp.authPassword(username, password)
        else {
            val certLocation = conf.certificateLocation
            val key = if (password.isEmpty()) {
                sshClientTemp.loadKeys(certLocation)
            } else {
                sshClientTemp.loadKeys(certLocation, password)
            }
            sshClientTemp.authPublickey(username, key)
        }

        sshClient = sshClientTemp
    }

    companion object {
        fun getInstance(project: Project): RPIConnector = project.service()
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}