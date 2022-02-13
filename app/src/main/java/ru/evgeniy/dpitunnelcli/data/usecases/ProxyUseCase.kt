package ru.evgeniy.dpitunnelcli.data.usecases

import com.topjohnwu.superuser.Shell
import ru.evgeniy.dpitunnelcli.domain.usecases.IProxyUseCase

class ProxyUseCase: IProxyUseCase {
    override fun set(ip: String, port: Int) {
        Shell.su("settings put global http_proxy $ip:$port").exec()
    }

    override fun unset() {
        Shell.su("settings put global http_proxy :0").exec()
    }
}