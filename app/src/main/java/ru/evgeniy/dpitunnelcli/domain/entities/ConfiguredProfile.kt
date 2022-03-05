package ru.evgeniy.dpitunnelcli.domain.entities

data class ConfiguredProfile(
    var wrongSeq: Boolean,
    var autoTtl: Boolean,
    var fakePacketsTtl: Int?,
    var windowSize: Int?,
    var windowScaleFactor: Int?,
    var desyncAttacks: Boolean,
    var desyncZeroAttack: DesyncZeroAttack?,
    var desyncFirstAttack: DesyncFirstAttack?
)