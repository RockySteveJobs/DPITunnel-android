package ru.evgeniy.dpitunnelcli.domain.usecases

import androidx.lifecycle.LiveData
import ru.evgeniy.dpitunnelcli.domain.entities.Profile

interface IFetchAllProfilesUseCase {
    suspend fun fetch(): List<Profile>
    fun fetchLive(): LiveData<List<Profile>>
}