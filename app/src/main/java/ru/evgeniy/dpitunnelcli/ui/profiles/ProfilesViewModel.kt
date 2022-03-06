package ru.evgeniy.dpitunnelcli.ui.profiles

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.evgeniy.dpitunnelcli.domain.entities.Profile
import ru.evgeniy.dpitunnelcli.domain.usecases.IDeleteProfileUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.IRenameProfileUseCase
import ru.evgeniy.dpitunnelcli.domain.usecases.ISettingsUseCase

class ProfilesViewModel(private val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                        private val deleteProfileUseCase: IDeleteProfileUseCase,
                        private val renameProfileUseCase: IRenameProfileUseCase,
                        private val settingsUseCase: ISettingsUseCase
) : ViewModel() {

    val profiles: LiveData<List<Profile>>

    init {
        profiles = fetchAllProfilesUseCase.fetchLive()
    }

    fun delete(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteProfileUseCase.delete(id)
        }
    }

    fun rename(id: Int, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            renameProfileUseCase.rename(id, newTitle)
        }
    }

    fun setDefaultProfile(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.setDefaultProfileId(id)
        }
    }
}