package com.github.madhattermonroe.rpilink.remote

import com.jcraft.jsch.ChannelExec
import java.io.ByteArrayOutputStream

object RemoteOperations {
    fun executeCommand(command: String): String? {
        return try {
            val session = SSHManager.getSession() ?: return null
            val channel = session.openChannel("exec") as ChannelExec
            channel.setCommand(command)

            val outputStream = ByteArrayOutputStream()
            channel.outputStream = outputStream
            channel.connect()

            while (!channel.isClosed) {
                Thread.sleep(100)
            }

            channel.disconnect()
            outputStream.toString().trim()
        } catch (e: Exception) {
            null
        }
    }
}