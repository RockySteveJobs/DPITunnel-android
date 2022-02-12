package ru.evgeniy.dpitunnelcli.ui.activity.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.evgeniy.dpitunnelcli.domain.usecases.*

class EditProfileViewModelFactory(val getDefaultIfaceUseCase: IFetchDefaultIfaceWifiAPUseCase,
                                  val autoConfigUseCase: IAutoConfigUseCase,
                                  val settingsUseCase: ISettingsUseCase,
                                  val saveProfileUseCase: ISaveProfileUseCase,
                                  val fetchProfileUseCase: IFetchProfileUseCase,
                                  val getStringResourceUseCase: IGetStringResourceUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IFetchDefaultIfaceWifiAPUseCase::class.java,
            IAutoConfigUseCase::class.java,
            ISettingsUseCase::class.java,
            ISaveProfileUseCase::class.java,
            IFetchProfileUseCase::class.java,
            IGetStringResourceUseCase::class.java)
            .newInstance(getDefaultIfaceUseCase, autoConfigUseCase, settingsUseCase, saveProfileUseCase, fetchProfileUseCase, getStringResourceUseCase)
    }
}