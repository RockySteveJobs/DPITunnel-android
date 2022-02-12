package ru.evgeniy.dpitunnelcli.domain.usecases

interface IGetStringResourceUseCase {
    fun getString(res: Int): String
}