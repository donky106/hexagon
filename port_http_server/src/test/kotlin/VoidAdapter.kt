package com.hexagonkt.http.server

object VoidAdapter : ServerPort {
    private var started = false

    override fun runtimePort() = 12345
    override fun started() = started
    override fun startup(server: Server) { started = true }
    override fun shutdown() { started = false }
}
