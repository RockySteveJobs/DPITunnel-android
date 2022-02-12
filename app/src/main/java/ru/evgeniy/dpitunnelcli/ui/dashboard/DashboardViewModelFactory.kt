package ru.evgeniy.dpitunnelcli.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.evgeniy.dpitunnelcli.domain.usecases.IDaemonUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IProxyUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.ISettingsUseCase

class DashboardViewModelFactory(val daemonUseCase: IDaemonUseCase,
                                val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                                val settingsUseCase: ISettingsUseCase,
                                val proxyUseCase: IProxyUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IDaemonUseCase::class.java,
            IFetchAllProfilesUseCase::class.java,
            ISettingsUseCase::class.java,
            IProxyUseCase::class.java)
            .newInstance(daemonUseCase,
                fetchAllProfilesUseCase,
                settingsUseCase,
                proxyUseCase)
    }
}