package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.database.AppDatabase
import ru.evgeniy.dpitunnelcli.domain.usecases.IDeleteProfileUseCase

class DeleteProfileUseCase(private val context: Context): IDeleteProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun delete(name: String) {
        profileDao.delete(name)
    }
}