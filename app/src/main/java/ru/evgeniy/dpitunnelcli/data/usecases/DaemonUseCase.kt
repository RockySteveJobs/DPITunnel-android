package ru.evgeniy.dpitunnelcli.data.usecases

import ru.evgeniy.dpitunnelcli.cli.CliDaemon
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.IDaemonUseCase

class DaemonUseCase(private val execPath: String,
                    private val pidFilePath: String): IDaemonUseCase {
    private val cliDaemon = CliDaemon(execPath, pidFilePath)
    override val daemonState = cliDaemon.daemonState

    override fun check() {
        cliDaemon.check()
    }

    override fun start(persistentOptions: CliDaemon.PersistentOptions, profiles: List<Profile>) {
        cliDaemon.start(persistentOptions, profiles)
    }

    override fun stop() {
        cliDaemon.stop()
    }
}