package ru.evgeniy.dpitunnelcli.ui.activity.editProfile

import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.evgeniy.dpitunnelcli.R
import ru.evgeniy.dpitunnelcli.domain.entities.*
import ru.evgeniy.dpitunnelcli.domain.usecases.*
import ru.evgeniy.dpitunnelcli.utils.AutoConfigOutputFilter
import ru.evgeniy.dpitunnelcli.utils.Constants

class EditProfileViewModel(private val fetchDefaultIfaceWifiAPUseCase: IFetchDefaultIfaceWifiAPUseCase,
                           private val autoConfigUseCase: IAutoConfigUseCase,
                           private val settingsUseCase: ISettingsUseCase,
                           private val saveProfileUseCase: ISaveProfileUseCase,
                           private val fetchProfileUseCase: IFetchProfileUseCase,
                           private val getStringResourceUseCase: IGetStringResourceUseCase
) : ViewModel() {

    private val _autoConfigOutput = MutableLiveData<String>()
    val autoConfigOutput: LiveData<String> get() = _autoConfigOutput

    private val _outputFiler = AutoConfigOutputFilter(AutoConfigOutputFilter.ResourceStrings(
        enterSite = getStringResourceUseCase.getString(R.string.autoconfig_enter_site),
        configureSuccessful = getStringResourceUseCase.getString(R.string.autoconfig_configuration_successful)
    )) { input -> autoConfigUseCase.input(input) }
    private var _profileCurrent: Profile? = null
        @Synchronized
        get() = field
        @Synchronized
        set(value) {
            field = value
            value?.let { _profile.postValue(it) }
        }

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> get() = _profile

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    var profileId: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.id = value
            }
        }
    var zeroAttack: Int
        get() = 0
        set(value) {
            _profileCurrent?.let {
                it.desyncAttacks = true
                it.desyncZeroAttack = if (value == 0) null else DesyncZeroAttack.values()[value.minus(1)]
            }
        }
    var firstAttack: Int
        get() = 0
        set(value) {
            _profileCurrent?.let {
                it.desyncAttacks = true
                it.desyncFirstAttack = if (value == 0) null else DesyncFirstAttack.values()[value.minus(1)]
            }
        }
    var ttl: String
        get() = ""
        set(value) {
            if (value.isEmpty()) return
            _profileCurrent?.let {
                it.fakePacketsTtl = value.toInt()
            }
        }
    var windowSize: String
        get() = ""
        set(value) {
            if (value.isEmpty()) return
            _profileCurrent?.let {
                it.windowSize = value.toInt()
            }
        }
    var windowScaleFactor: String
        get() = ""
        set(value) {
            if (value.isEmpty()) return
            _profileCurrent?.let {
                it.windowScaleFactor = value.toInt()
            }
        }
    var splitPosition: String
        get() = ""
        set(value) {
            if (value.isEmpty()) return
            _profileCurrent?.let {
                it.splitPosition = value.toInt()
            }
        }
    var splitAtSNI: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.splitAtSni = value
            }
        }
    var doh: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.doh = value
            }
        }
    var dohServer: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.dohServer = value.ifEmpty { null }
            }
        }
    var dnsServer: String
        get() = ""
        set(value) {
            _profileCurrent?.let { it ->
                if (value.isEmpty()) {
                    it.inBuiltDNS = true
                    it.inBuiltDNSIP = Constants.DEFAULT_PROFILE.inBuiltDNSIP
                    it.inBuiltDNSPort = null
                }
                else {
                    it.inBuiltDNS = true
                    it.inBuiltDNSIP = value.split(':').getOrNull(0)
                    it.inBuiltDNSPort = value.split(':').getOrNull(1)?.toIntOrNull()
                }
            }
        }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            autoConfigUseCase.inputFlow.collect {
                _outputFiler.feed(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _outputFiler.configuredProfile.collect { state ->
                when(state) {
                    is AutoConfigOutputFilter.ConfiguredProfileState.Success -> loadConfiguredProfile(state.configuredProfile)
                    is AutoConfigOutputFilter.ConfiguredProfileState.Error -> {}
                    is AutoConfigOutputFilter.ConfiguredProfileState.InProcess -> {}
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _outputFiler.output.collect {
                it?.let { _autoConfigOutput.postValue(it) }
            }
        }
    }

    fun save() {
        _profileCurrent?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (it.id.isBlank()) {
                    _uiState.postValue(UIState.Error(ErrorType.ERROR_TYPE_INVALID_PROFILE_ID))
                    return@launch
                }
                saveProfileUseCase.save(it)
                _uiState.postValue(UIState.Finish)
            }
        }
    }

    fun getDefaultIfaceWifiAP(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _profileCurrent?.let {
                val profile = it
                profile.id = fetchDefaultIfaceWifiAPUseCase.fetch(context).let {
                    it.first?.plus(it.second?.let {
                        ":$it"
                    } ?: "") ?: ""
                }
                _profileCurrent = profile
            }
        }
    }

    fun runAutoConfig(context: Context) {
        _outputFiler.reset(AutoConfigDefaults(
            caBundlePath = settingsUseCase.getCABundlePath()!!,
            dohServer = _profileCurrent?.dohServer ?: Constants.DEFAULT_PROFILE.dohServer!!,
            inBuiltDNS = _profileCurrent?.inBuiltDNSIP?.plus(_profileCurrent?.inBuiltDNSPort?.let { ":$it" } ?: "") ?: Constants.DEFAULT_PROFILE.inBuiltDNSIP!!
        ))
        autoConfigUseCase.run(
            viewModelScope,
            listOf(
                "su",
                "-c",
                context.applicationInfo.nativeLibraryDir + '/' + Constants.DPITUNNEL_BINARY_NAME + ' ' +
                        "--auto"
            )
        ) { throwable ->
            _autoConfigOutput.postValue(throwable.stackTraceToString())
        }
    }

    fun showFullLog(isShow: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            if (isShow)
                _outputFiler.showFull()
            else
                _outputFiler.showSimple()
        }
    }

    fun autoConfigSendInput(input: String) {
        _outputFiler.input(input)
    }

    fun loadProfile(profileId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            loadProfile(profileId?.let {
                fetchProfileUseCase.fetch(it)
            })
        }
    }

    private fun loadProfile(profile: Profile?) {
        if (profile == null)
            _profileCurrent = Constants.DEFAULT_PROFILE.copy()
        else
            _profileCurrent = profile
    }

    private fun loadConfiguredProfile(configuredProfile: ConfiguredProfile) {
        _profileCurrent?.let {
            val profile = it
            profile.fakePacketsTtl = configuredProfile.fakePacketsTtl
            profile.windowSize = configuredProfile.windowSize
            profile.windowScaleFactor = configuredProfile.windowScaleFactor
            profile.desyncAttacks = configuredProfile.desyncAttacks
            profile.desyncZeroAttack = configuredProfile.desyncZeroAttack
            profile.desyncFirstAttack = configuredProfile.desyncFirstAttack
            _profileCurrent = it
        }
    }

    enum class ErrorType {
        ERROR_TYPE_INVALID_PROFILE_ID
    }

    sealed class UIState {
        object Normal: UIState()
        data class Error(val error: ErrorType, val errorString: String? = null): UIState()
        object Finish: UIState()
    }
}