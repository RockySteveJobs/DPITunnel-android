package ru.evgeniy.dpitunnelcli.domain.entities

data class AutoConfigDefaults(
    val caBundlePath: String,
    val dohServer: String,
    val inBuiltDNS: String
)
