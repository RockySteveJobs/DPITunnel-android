package ru.evgeniy.dpitunnelcli.domain.usecases

interface IRenameProfileUseCase {
    suspend fun rename(name: String, newTitle: String)
}