package com.michaldrabik.showly2.ui.settings

import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
  private val interactor: SettingsInteractor
) : BaseViewModel<SettingsUiModel>() {

}
