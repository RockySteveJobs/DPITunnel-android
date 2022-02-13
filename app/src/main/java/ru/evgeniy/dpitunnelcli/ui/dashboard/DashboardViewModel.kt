package ru.evgeniy.dpitunnelcli.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ru.evgeniy.dpitunnelcli.cli.CliDaemon
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.*
import ru.evgeniy.dpitunnelcli.utils.Constants

class DashboardViewModel(private val daemonUseCase: IDaemonUseCase,
                         private val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                         private val settingsUseCase: ISettingsUseCase,
                         private val proxyUseCase: IProxyUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    private var lastDaemonState: DaemonState? = null
    private var _profiles: List<Profile>? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            daemonUseCase.daemonState.collect { state ->
                when(state) {
                    is DaemonState.Loading -> {
                        _uiState.postValue(UIState.Loading)
                    }
                    is DaemonState.Running -> {
                        _uiState.postValue(UIState.Running)
                        if (lastDaemonState is DaemonState.Stopped || lastDaemonState is DaemonState.Error)
                            proxyUseCase.set("127.0.0.1", settingsUseCase.getPort() ?: Constants.DPITUNNEL_DEFAULT_PORT)
                    }
                    is DaemonState.Stopped -> {
                        _uiState.postValue(UIState.Stopped)
                        if (lastDaemonState is DaemonState.Running)
                            proxyUseCase.unset()
                    }
                    is DaemonState.Error -> {
                        _uiState.postValue(UIState.Stopped)
                    }
                }
                lastDaemonState = state
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _profiles = fetchAllProfilesUseCase.fetch()
        }
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                daemonUseCase.check()
                delay(2000)
            }
        }
    }

    fun startStop() {
        _profiles?.let { _profiles ->
            viewModelScope.launch(Dispatchers.IO) {
                when(daemonUseCase.daemonState.value) {
                    is DaemonState.Running -> daemonUseCase.stop()
                    is DaemonState.Stopped -> daemonUseCase.start(
                        CliDaemon.PersistentOptions(
                            caBundlePath = settingsUseCase.getCABundlePath()!!,
                            ip = settingsUseCase.getIP(),
                            port = settingsUseCase.getPort()
                        ),
                        _profiles
                    )
                    is DaemonState.Error -> {}
                }
            }
        }
    }

    fun restart() {
        _profiles?.let { _profiles ->
            viewModelScope.launch(Dispatchers.IO) {
                daemonUseCase.stop()
                var isStopped = false
                withTimeoutOrNull(2000) {
                    daemonUseCase.daemonState.collect { state ->
                        if (state is DaemonState.Stopped)
                            isStopped = true
                    }
                }
                if (isStopped)
                    daemonUseCase.start(
                        CliDaemon.PersistentOptions(
                            caBundlePath = settingsUseCase.getCABundlePath()!!,
                            ip = settingsUseCase.getIP(),
                            port = settingsUseCase.getPort()
                        ),
                        _profiles
                    )
            }
        }
    }

    sealed class UIState {
        object Running: UIState()
        object Stopped: UIState()
        object Loading: UIState()
    }
}