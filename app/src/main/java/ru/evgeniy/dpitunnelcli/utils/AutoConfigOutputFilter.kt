package ru.evgeniy.dpitunnelcli.utils

import kotlinx.coroutines.flow.*
import ru.evgeniy.dpitunnelcli.domain.entities.AutoConfigDefaults
import ru.evgeniy.dpitunnelcli.domain.entities.ConfiguredProfile
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncFirstAttack
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncZeroAttack

class AutoConfigOutputFilter(private val resourceStrings: ResourceStrings, private val _input: (String) -> Unit) {

    private val _output = StringBuffer()
    private val _outputSimple = StringBuffer()
    private var _enableSimpleOutput = true

    private var _defaults: AutoConfigDefaults? = null

    private val _outputFlow = MutableStateFlow<String?>(null)
    val output: StateFlow<String?> = _outputFlow

    private val _configuredProfile = MutableStateFlow<ConfiguredProfileState>(ConfiguredProfileState.InProcess)
    val configuredProfile: StateFlow<ConfiguredProfileState> = _configuredProfile

    fun feed(output: String) {
        _output.append(output)
        processOutput()
        _outputFlow.value = if (_enableSimpleOutput)
            _outputSimple.toString()
        else
            _output.toString()
    }
    
    fun reset(defaults: AutoConfigDefaults? = null) {
        defaults?.let { _defaults = defaults }
        _output.setLength(0)
        _outputSimple.setLength(0)
    }

    fun input(input: String, isAutoComplete: Boolean = false) {
        _output.append(input)
        if (!isAutoComplete)
            _outputSimple.append(input)

        _outputFlow.value = if (_enableSimpleOutput)
            _outputSimple.toString()
        else
            _output.toString()

        _input(input)
    }

    fun showFull() {
        _enableSimpleOutput = false
        _outputFlow.value = _output.toString()
    }

    fun showSimple() {
        _enableSimpleOutput = true
        _outputFlow.value = _outputSimple.toString()
    }

    private fun processOutput() {
        with(_output.lines()) {
            when {
                last().startsWith(ENTER_SITE_PROMPT) -> _outputSimple.append("${resourceStrings.enterSite}\n")
                last().startsWith(DOH_PROMPT) -> _defaults?.dohServer?.let { input("$it\n", true) }
                last().startsWith(CA_BUNDLE_PROMPT) -> _defaults?.caBundlePath?.let { input("$it\n", true) }
                last().startsWith(INBUILT_DNS_PROMPT) -> _defaults?.inBuiltDNS?.let { input("$it\n", true) }
                getOrNull(size - 3)?.startsWith(SUCCESSFUL_PROMPT) == true -> { // 3rd line from end
                    val ret = parseOptions()
                    if (ret == null) {
                        _configuredProfile.value = ConfiguredProfileState.Error
                    }
                    else {
                        _outputSimple.append("${resourceStrings.configureSuccessful}\n")
                        _configuredProfile.value = ConfiguredProfileState.Success(ret)
                    }
                }
                else -> {}
            }
        }
    }

    private fun parseOptions(): ConfiguredProfile? {
        // 2nd line from end
        _output.lines().let { it.getOrNull(it.size - 2) }?.split(' ')?.let { argsStr ->
            val configuredProfile = ConfiguredProfile(null, null, null, false, null, null)
            val args = Utils.getOpt(argsStr)
            for ((key,value) in args) {
                when(key) {
                    "-desync-attacks" -> {
                        configuredProfile.desyncAttacks = true
                        value.first().split(',').forEach { when(it){
                            "fake" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_FAKE
                            "rst" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_RST
                            "rstack" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_RSTACK
                            "disorder" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER
                            "disorder_fake" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER_FAKE
                            "split" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_SPLIT
                            "split_fake" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_SPLIT_FAKE
                        } }
                    }
                    "-wsize" -> configuredProfile.windowSize = value.first().toInt()
                    "-wsfactor" -> configuredProfile.windowScaleFactor = value.first().toInt()
                    "-ttl" -> configuredProfile.fakePacketsTtl = value.first().toInt()
                }
            }
            return configuredProfile
        }
        return null
    }

    data class ResourceStrings(
        val enterSite: String,
        val configureSuccessful: String
    )

    sealed class ConfiguredProfileState {
        object InProcess : ConfiguredProfileState()
        data class Success(val configuredProfile: ConfiguredProfile): ConfiguredProfileState()
        object Error : ConfiguredProfileState()
    }

    companion object {
        private const val ENTER_SITE_PROMPT = "(http://example.com or https://example.com or example.com. Can contain port): "
        private const val DOH_PROMPT = "DoH server (press enter to use default"
        private const val CA_BUNDLE_PROMPT = "CA bundle path (press enter to use default location"
        private const val INBUILT_DNS_PROMPT = "DNS server (press enter to use default "
        private const val SUCCESSFUL_PROMPT = "Configuration successful! Apply these options when run program:"
    }
}