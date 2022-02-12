package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.database.AppDatabase
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncFirstAttack
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncZeroAttack
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase

class FetchAllProfilesUseCase(private val context: Context): IFetchAllProfilesUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun fetch(): List<Profile> {
        val newList = mutableListOf<Profile>()
        profileDao.getAll().forEach { profile ->
            newList += Profile(
                id = profile.id,
                title = profile.title,
                bufferSize = profile.bufferSize,
                splitPosition = profile.splitPosition,
                splitAtSni = profile.splitAtSni,
                fakePacketsTtl = profile.fakePacketsTtl,
                windowSize = profile.windowSize,
                windowScaleFactor = profile.windowScaleFactor,
                inBuiltDNS = profile.inBuiltDNS,
                inBuiltDNSIP = profile.inBuiltDNSIP,
                inBuiltDNSPort = profile.inBuiltDNSPort,
                doh = profile.doh,
                dohServer = profile.dohServer,
                desyncAttacks = profile.desyncAttacks,
                desyncZeroAttack = profile.desyncZeroAttack?.ordinal?.let { DesyncZeroAttack.values()[it] },
                desyncFirstAttack = profile.desyncFirstAttack?.ordinal?.let { DesyncFirstAttack.values()[it] }
            )
        }
        return newList
    }
}