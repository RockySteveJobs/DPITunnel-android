package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.database.AppDatabase
import ru.evgeniy.dpitunnelcli.domain.usecases.IRenameProfileUseCase

class RenameProfileUseCase(private val context: Context): IRenameProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun rename(name: String, newTitle: String) {
        profileDao.rename(name, newTitle)
    }
}