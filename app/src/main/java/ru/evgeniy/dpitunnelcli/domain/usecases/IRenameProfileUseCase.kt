package ru.evgeniy.dpitunnelcli.domain.usecases

interface IRenameProfileUseCase {
    suspend fun rename(id: Int, newTitle: String)
}