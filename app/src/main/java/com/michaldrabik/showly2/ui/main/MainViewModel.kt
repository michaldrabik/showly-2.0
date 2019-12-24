package com.michaldrabik.showly2.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

class MainViewModel @Inject constructor(
  private val interactor: MainInteractor
) : BaseViewModel<MainUiModel>() {

  fun initSettings() {
    viewModelScope.launch {
      interactor.initSettings()
      checkInitialRun()
      interactor.initFcm()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = interactor.isInitialRun()
    if (isInitialRun) interactor.setInitialRun(false)
    uiState = MainUiModel(isInitialRun = isInitialRun)
  }

  fun refreshAnnouncements(context: Context) {
    viewModelScope.launch {
      interactor.refreshAnnouncements(context)
    }
  }

  fun clearCache() = interactor.clearCache()
}
