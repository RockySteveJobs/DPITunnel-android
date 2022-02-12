package ru.evgeniy.dpitunnelcli.domain.usecases

interface ISettingsUseCase {
    fun getStartOnBoot(): Boolean
    fun getCABundlePath(): String?
    fun getIP(): String?
    fun getPort(): Int?
}