package com.michaldrabik.showly2.ui.main

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val interactor: MainInteractor
) : BaseViewModel<MainUiModel>() {

  fun initSettings() {
    viewModelScope.launch {
      interactor.initSettings()
      checkInitialRun()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = interactor.isInitialRun()
    if (isInitialRun) interactor.setInitialRun(false)
    _uiStream.value = MainUiModel(isInitialRun = isInitialRun)
  }

  fun clearCache() = interactor.clearCache()
}
