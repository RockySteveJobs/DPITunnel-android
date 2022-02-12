package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.domain.usecases.ISettingsUseCase
import ru.evgeniy.dpitunnelcli.preferences.AppPreferences

class SettingsUseCase(private val context: Context): ISettingsUseCase {
    private val appPreferences = AppPreferences.getInstance(context)

    override fun getStartOnBoot(): Boolean = appPreferences.startOnBoot
    override fun getCABundlePath() = appPreferences.caBundlePath
    override fun getIP(): String? = appPreferences.ip
    override fun getPort(): Int? = appPreferences.port
}