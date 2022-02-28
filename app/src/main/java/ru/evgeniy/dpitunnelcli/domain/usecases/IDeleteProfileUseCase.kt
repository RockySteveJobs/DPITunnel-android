package ru.evgeniy.dpitunnelcli.domain.usecases

interface IDeleteProfileUseCase {
    suspend fun delete(id: Int)
}