package com.michaldrabik.showly2.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Tip
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

  fun isTipShown(tip: Tip) = interactor.isTutorialShown(tip)

  fun setTipShown(tip: Tip) = interactor.setTutorialShown(tip)

  fun clearUp() = interactor.clear()
}
