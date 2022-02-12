package ru.evgeniy.dpitunnelcli.data.usecases

import android.content.Context
import ru.evgeniy.dpitunnelcli.domain.usecases.IGetStringResourceUseCase

class GetStringResourceUseCase(private val context: Context): IGetStringResourceUseCase {
    override fun getString(res: Int): String = context.getString(res)
}