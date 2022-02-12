package ru.evgeniy.dpitunnelcli.cli

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.DaemonState

class CliDaemon(private val execPath: String,
                private val pidFilePath: String) {

    private val _daemonState = MutableStateFlow<DaemonState>(DaemonState.Loading)
    val daemonState: StateFlow<DaemonState> = _daemonState

    fun check() {
        kotlin.runCatching {
            val p = ProcessBuilder(listOf("su",
                "-c",
                "[ -f /proc/\$(cat \"$pidFilePath\")/status ]"))
                .start()
            p.waitFor()
            return if (p.exitValue() == 0)
                _daemonState.value = DaemonState.Running
            else
                _daemonState.value = DaemonState.Stopped
        }.onFailure { e -> _daemonState.value = DaemonState.Error(e) }
    }

    fun start(persistentOptions: PersistentOptions, profiles: List<Profile>) {
        val argsStr = StringBuilder()
        argsStr.append("$execPath --pid \"$pidFilePath\"")
        argsStr.append(' ')
        argsStr.append(persistentOptions.toString())
        /* This variable checks for "default" profile
        * if there is no default profile, then DPITunnel-cli binary will crash
        * when user will connect to network that we don't have profile for
        * so just add dummy default profile, that won't apply any censor bypass techniques */
        var isDefaultExists = false
        profiles.forEach {
            if(it.id == "default")
                isDefaultExists = true
            argsStr.append(' ')
            argsStr.append(it.toString())
        }
        if (!isDefaultExists) {
            argsStr.append(" --profile default") // Dummy "default" profile
        }
        kotlin.runCatching {
            ProcessBuilder(listOf("su", "-c", argsStr.toString()))
                .redirectErrorStream(true)
                .start()
        }.onFailure { e -> _daemonState.value = DaemonState.Error(e) }
    }

    fun stop() {
        kotlin.runCatching {
            val p = ProcessBuilder(listOf("su",
                "-c",
                "kill -INT \$(cat \"$pidFilePath\")"))
                .start()
            p.waitFor()
        }.onFailure { e -> _daemonState.value = DaemonState.Error(e) }
    }

    data class PersistentOptions(
        val caBundlePath: String,
        val ip: String?,
        val port: Int?
    ) {
        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("--daemon --ca-bundle-path \"${this.caBundlePath}\"")
            ip?.let { stringBuilder.append(" --ip $it") }
            port?.let { stringBuilder.append(" --port $it") }
            return stringBuilder.toString()
        }
    }
}