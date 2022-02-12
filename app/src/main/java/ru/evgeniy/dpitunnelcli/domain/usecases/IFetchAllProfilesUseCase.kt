package ru.evgeniy.dpitunnelcli.domain.usecases

import ru.evgeniy.dpitunnelcli.domain.entities.Profile

interface IFetchAllProfilesUseCase {
    suspend fun fetch(): List<Profile>
}