package ru.evgeniy.dpitunnelcli.domain.usecases

interface IProxyUseCase {
    fun set(ip: String, port: Int)
    fun unset()
}