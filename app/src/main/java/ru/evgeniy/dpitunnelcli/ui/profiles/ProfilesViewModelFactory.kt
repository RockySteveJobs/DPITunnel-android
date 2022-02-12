package ru.evgeniy.dpitunnelcli.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.evgeniy.dpitunnelcli.domain.usecases.IDeleteProfileUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IRenameProfileUseCase

class ProfilesViewModelFactory(val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                               val deleteProfileUseCase: IDeleteProfileUseCase,
                               val renameProfileUseCase: IRenameProfileUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IFetchAllProfilesUseCase::class.java,
            IDeleteProfileUseCase::class.java,
            IRenameProfileUseCase::class.java)
            .newInstance(fetchAllProfilesUseCase,
                deleteProfileUseCase,
                renameProfileUseCase)
    }
}