package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.database.AppDatabase
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncFirstAttack
import ru.evgeniy.dpitunnelcli.domain.entities.DesyncZeroAttack
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase
import ru.evgeniy.dpitunnelcli.preferences.AppPreferences

class FetchAllProfilesUseCase(private val context: Context): IFetchAllProfilesUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()
    private val appPreferences = AppPreferences.getInstance(context)

    override suspend fun fetch(): List<Profile> {
        val newList = mutableListOf<Profile>()
        val defaultProfileId = appPreferences.defaultProfileId
        var defaultExist = false
        profileDao.getAll().forEach { profile ->
            if (profile.id == defaultProfileId)
                defaultExist = true
            newList += Profile(
                id = profile.id,
                name = profile.name,
                title = profile.title,
                bufferSize = profile.bufferSize,
                splitPosition = profile.splitPosition,
                splitAtSni = profile.splitAtSni,
                autoTtl= profile.autoTtl,
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
                desyncFirstAttack = profile.desyncFirstAttack?.ordinal?.let { DesyncFirstAttack.values()[it] },
                default = profile.id == defaultProfileId
            )
        }

        if (!defaultExist) {
            newList.getOrNull(0)?.let {
                it.default = true
                appPreferences.defaultProfileId = it.id
            }
        }
        return newList
    }
}