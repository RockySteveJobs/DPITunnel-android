package ru.evgeniy.dpitunnelcli.utils

import kotlinx.coroutines.flow.*
import ru.evgeniy.dpitunnelcli.domain.entities.AutoConfigDefaults
import ru.evgeniy.dpitunnelcli.domain.entities.ConfiguredProfile
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncFirstAttack
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncZeroAttack

class AutoConfigOutputFilter(private val _input: (String) -> Unit) {

    private val _output = StringBuffer()

    private var _defaults: AutoConfigDefaults? = null

    private val _outputFlow = MutableStateFlow<String?>(null)
    val output: StateFlow<String?> = _outputFlow

    private val _configuredProfile = MutableStateFlow<ConfiguredProfileState>(ConfiguredProfileState.Stopped)
    val configuredProfile: StateFlow<ConfiguredProfileState> = _configuredProfile

    fun feed(output: String) {
        _output.append(output)
        processOutput()
        _outputFlow.value = _output.toString()
    }
    
    fun reset(defaults: AutoConfigDefaults? = null) {
        defaults?.let { _defaults = defaults }
        _output.setLength(0)
        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.ZERO)
    }

    fun input(input: String) {
        _output.append(input)

        _outputFlow.value = _output.toString()

        _input(input)
    }

    private fun processOutput() {
        with(_output.lines()) {
            when {
                last().startsWith(ENTER_SITE_PROMPT) -> input("${_defaults?.domain}\n")
                last().startsWith(DOH_PROMPT) -> input("${_defaults?.dohServer}\n")
                last().startsWith(CA_BUNDLE_PROMPT) -> input("${_defaults?.caBundlePath}\n")
                last().startsWith(INBUILT_DNS_PROMPT) -> input("${_defaults?.inBuiltDNS}\n")
                getOrNull(size - 2)?.startsWith(FAIL_RESOLVE_HOST_PROMPT) == true -> { // 1nd line from end
                    _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_RESOLVE_DOMAIN_FAILED)
                }
                getOrNull(size - 3)?.startsWith(SUCCESSFUL_PROMPT) == true -> { // 2nd line from end
                    val ret = parseOptions()
                    if (ret == null) {
                        _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_CONFIG_PARSE_FAILED)
                    }
                    else {
                        _configuredProfile.value = ConfiguredProfileState.Success(ret)
                    }
                }
                // Last attack
                lastIndexOf(RST_ACK_AND_DISORDER_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(RST_ACK_AND_DISORDER_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST_ACK_AND_DISORDER_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST_ACK_AND_DISORDER_FAKE)
                    // If last attack failed
                    val lastLine = getOrNull(size - 3) // 2nd line from end
                    if (lastLine?.startsWith(FAIL_PROMPT) == true)
                        _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_NO_ATTACKS_FOUND)
                }
                lastIndexOf(RST_AND_DISORDER_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(RST_AND_DISORDER_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST_AND_DISORDER_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST_AND_DISORDER_FAKE)
                }
                lastIndexOf(FAKE_AND_DISORDER_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(FAKE_AND_DISORDER_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.FAKE_AND_DISORDER_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.FAKE_AND_DISORDER_FAKE)
                }
                lastIndexOf(FAKE_AND_SPLIT_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(FAKE_AND_SPLIT_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.FAKE_AND_SPLIT_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.FAKE_AND_SPLIT_FAKE)
                }
                lastIndexOf(RST_ACK_ATTACK) != -1 -> {
                    _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST_ACK)
                }
                lastIndexOf(RST_ATTACK) != -1 -> {
                    _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.RST)
                }
                lastIndexOf(FAKE_ATTACK) != -1 -> {
                    _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.FAKE)
                }
                lastIndexOf(SPLIT_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(SPLIT_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT_FAKE)
                }
                lastIndexOf(SPLIT_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(SPLIT_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT_FAKE)
                }
                lastIndexOf(DISORDER_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(DISORDER_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER_FAKE)
                }
                lastIndexOf(DISORDER_FAKE_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(DISORDER_FAKE_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER_FAKE_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER_FAKE)
                }
                lastIndexOf(DISORDER_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(DISORDER_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.DISORDER)
                }
                lastIndexOf(SPLIT_ATTACK) != -1 -> {
                    if (getOrNull(lastIndexOf(SPLIT_ATTACK) + 1)?.equals(WITH_LOW_TCP_WIN_ATTACK) == true)
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT_LOW_WIN)
                    else
                        _configuredProfile.value = ConfiguredProfileState.InProcess(Progress.SPLIT)
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

    enum class Progress {
        ZERO,
        SPLIT,
        SPLIT_LOW_WIN,
        DISORDER,
        DISORDER_LOW_WIN,
        DISORDER_FAKE,
        DISORDER_FAKE_LOW_WIN,
        SPLIT_FAKE,
        SPLIT_FAKE_LOW_WIN,
        FAKE,
        RST,
        RST_ACK,
        FAKE_AND_SPLIT_FAKE,
        FAKE_AND_SPLIT_FAKE_LOW_WIN,
        FAKE_AND_DISORDER_FAKE,
        FAKE_AND_DISORDER_FAKE_LOW_WIN,
        RST_AND_DISORDER_FAKE,
        RST_AND_DISORDER_FAKE_LOW_WIN,
        RST_ACK_AND_DISORDER_FAKE,
        RST_ACK_AND_DISORDER_FAKE_LOW_WIN
    }

    enum class ErrorType {
        ERROR_NO_ATTACKS_FOUND,
        ERROR_RESOLVE_DOMAIN_FAILED,
        ERROR_CONFIG_PARSE_FAILED
    }

    sealed class ConfiguredProfileState {
        data class InProcess(val progress: Progress): ConfiguredProfileState()
        data class Success(val configuredProfile: ConfiguredProfile): ConfiguredProfileState()
        data class Error(val error: ErrorType) : ConfiguredProfileState()
        object Stopped: ConfiguredProfileState()
    }

    companion object {
        private const val ENTER_SITE_PROMPT = "(http://example.com or https://example.com or example.com. Can contain port): "
        private const val DOH_PROMPT = "DoH server (press enter to use default"
        private const val CA_BUNDLE_PROMPT = "CA bundle path (press enter to use default location"
        private const val INBUILT_DNS_PROMPT = "DNS server (press enter to use default "
        private const val SPLIT_ATTACK = "\tTrying split attack..."
        private const val DISORDER_ATTACK = "\tTrying disorder attack..."
        private const val DISORDER_FAKE_ATTACK = "\tTrying disorder(fake) attack..."
        private const val SPLIT_FAKE_ATTACK = "\tTrying split(fake) attack..."
        private const val FAKE_ATTACK = "\tTrying fake packet attack..."
        private const val RST_ATTACK = "\tTrying RST attack..."
        private const val RST_ACK_ATTACK = "\tTrying RST, ACK attack..."
        private const val FAKE_AND_SPLIT_FAKE_ATTACK = "\tTrying fake packet + split(fake) attacks..."
        private const val FAKE_AND_DISORDER_FAKE_ATTACK = "\tTrying fake packet + disorder(fake) attacks..."
        private const val RST_AND_DISORDER_FAKE_ATTACK = "\tTrying RST packet + disorder(fake) attacks..."
        private const val RST_ACK_AND_DISORDER_FAKE_ATTACK = "\tTrying RST, ACK packet + disorder(fake) attacks..."
        private const val WITH_LOW_TCP_WIN_ATTACK = "\t(set low TCP window size)"

        private const val SUCCESSFUL_PROMPT = "Configuration successful! Apply these options when run program:"
        private const val FAIL_PROMPT = "\tFail"
        private const val FAIL_RESOLVE_HOST_PROMPT = "Failed to resolve host "
    }
}