package ru.evgeniy.dpitunnelcli.data.usecases

import ru.evgeniy.dpitunnelcli.domain.usecases.IProxyUseCase

class ProxyUseCase: IProxyUseCase {
    override fun set(ip: String, port: Int) {
        kotlin.runCatching {
            val p = ProcessBuilder(listOf("su",
                "-c",
                "settings put global http_proxy $ip:$port"))
                .start()
            p.waitFor()
        }
    }

    override fun unset() {
        kotlin.runCatching {
            val p = ProcessBuilder(listOf("su",
                "-c",
                "settings put global http_proxy :0"))
                .start()
            p.waitFor()
        }
    }
}