package ru.evgeniy.dpitunnelcli.utils

import ru.evgeniy.dpitunnelcli.domain.entities.Profile

object Constants {
    val TTL_VALUE_RANGE = 1..255
    val TCP_WINDOW_SIZE_VALUE_RANGE = 1..65535
    val TCP_WINDOW_SCALE_FACTOR_VALUE_RANGE = 0..14
    val SPLIT_POSITION_VALUE_RANGE = 1..65535
    val SERVER_PORT_RANGE = 1..65535
    const val DPITUNNEL_DEFAULT_PORT = 8080
    const val DPITUNNEL_BINARY_NAME = "libdpitunnel-cli.so"
    const val DPITUNNEL_DAEMON_PID_FILE = "/data/local/tmp/dpitunnel-cli-daemon.pid"
    val DEFAULT_PROFILE = Profile(
        id = "",
        title = null,
        bufferSize = null,
        splitPosition = 3,
        splitAtSni = true,
        fakePacketsTtl = null,
        windowSize = null,
        windowScaleFactor = null,
        inBuiltDNS = true,
        inBuiltDNSIP = "8.8.8.8",
        inBuiltDNSPort = null,
        doh = true,
        dohServer = "https://dns.google/dns-query",
        desyncAttacks = false,
        desyncZeroAttack = null,
        desyncFirstAttack = null
    )
}