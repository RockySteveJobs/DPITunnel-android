package ru.evgeniy.dpitunnelcli.domain.usecases

import ru.evgeniy.dpitunnelcli.domain.entities.Profile

interface IFetchProfileUseCase {
    suspend fun fetch(id: Int): Profile?
}