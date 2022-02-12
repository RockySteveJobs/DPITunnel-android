package ru.evgeniy.dpitunnelcli.domain.usecases

import ru.evgeniy.dpitunnelcli.domain.entities.Profile

interface ISaveProfileUseCase {
    suspend fun save(profile: Profile)
}