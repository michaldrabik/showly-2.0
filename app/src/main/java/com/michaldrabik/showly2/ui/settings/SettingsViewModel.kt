package com.michaldrabik.showly2.ui.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
  private val interactor: SettingsInteractor
) : BaseViewModel<SettingsUiModel>() {

  fun loadSettings() {
    viewModelScope.launch {
      refreshSettings()
    }
  }

  fun setRecentShowsAmount(amount: Int) {
    viewModelScope.launch {
      interactor.setRecentShowsAmount(amount)
      refreshSettings()
    }
  }

  fun enablePushNotifications(enable: Boolean) {
    viewModelScope.launch {
      interactor.enablePushNotifications(enable)
      refreshSettings()
    }
  }

  fun enableShowsNotifications(enable: Boolean, context: Context) {
    viewModelScope.launch {
      interactor.enableShowsNotifications(enable, context)
      refreshSettings()
    }
  }

  private suspend fun refreshSettings() {
    uiState = SettingsUiModel(interactor.getSettings())
  }
}
