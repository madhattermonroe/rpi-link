package com.github.madhattermonroe.rpilink.remote

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

class RemoteOperations {

    private var session: Session? = null
    private var isConnected: Boolean = false

    fun startConnection(host: String, port: String, user: String, password: String): Boolean {
        return try {
            val jsch = JSch()
            session = jsch.getSession(user, host, port.toInt()).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                connect(5000)
            }
            isConnected = true
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSession(): Session? {
        return session
    }

    fun disconnect() {
        isConnected = false
        session?.disconnect()
    }
}