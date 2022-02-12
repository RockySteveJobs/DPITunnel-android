package ru.evgeniy.dpitunnelcli.domain.entities

data class ConfiguredProfile(
    var fakePacketsTtl: Int?,
    var windowSize: Int?,
    var windowScaleFactor: Int?,
    var desyncAttacks: Boolean,
    var desyncZeroAttack: DesyncZeroAttack?,
    var desyncFirstAttack: DesyncFirstAttack?
)