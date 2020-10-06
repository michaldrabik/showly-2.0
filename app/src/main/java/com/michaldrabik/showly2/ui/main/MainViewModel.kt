package com.michaldrabik.showly2.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.ui.main.cases.*
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Tip
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val initCase: MainInitialsCase,
  private val tipsCase: MainTipsCase,
  private val traktCase: MainTraktCase,
  private val miscCase: MainMiscCase,
  private val rateAppCase: MainRateAppCase
) : BaseViewModel<MainUiModel>() {

  fun initSettings() {
    viewModelScope.launch {
      initCase.initSettings()
      checkInitialRun()
      initCase.initFcm()
    }
  }

  private suspend fun checkInitialRun() {
    val isInitialRun = initCase.isInitialRun()
    if (isInitialRun) {
      initCase.setInitialRun(false)
    }
    val showWhatsNew = initCase.showWhatsNew(isInitialRun)
    val showRateApp = rateAppCase.shouldShowRateApp()
    uiState = MainUiModel(isInitialRun = isInitialRun, showWhatsNew = showWhatsNew, showRateApp = showRateApp)
  }

  fun refreshAnnouncements(context: Context) {
    viewModelScope.launch {
      miscCase.refreshAnnouncements(context)
    }
  }

  fun refreshTraktSyncSchedule(context: Context) {
    viewModelScope.launch {
      traktCase.run {
        refreshTraktSyncSchedule(context)
        refreshTraktQuickSync(context)
      }
    }
  }

  fun isTipShown(tip: Tip) = tipsCase.isTipShown(tip)

  fun setTipShown(tip: Tip) = tipsCase.setTipShown(tip)

  fun finishRateApp() = rateAppCase.finalize()

  fun clearUp() = miscCase.clear()
}
