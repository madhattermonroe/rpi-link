package com.github.madhattermonroe.rpilink.state

import com.intellij.openapi.components.BaseState

class ConnectionParams : BaseState() {
    var host by string()
    var port: Int by property(22)
    var username by string()
    var password by string()
    var autoConnect by property(false)
}